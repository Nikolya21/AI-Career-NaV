package org.example.aicareernav1.service.gigachat;

import chat.giga.client.GigaChatClient;
import chat.giga.model.ModelName;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GigaChatService {

  private final GigaChatClient gigaChatClient;

  // Семафор разрешает только 1 поток одновременно.
  // Это превращает хаотичные запросы в организованную очередь.
  private final Semaphore semaphore = new Semaphore(1);

  @Value("${gigachat.max-tokens:12000}")
  private int maxTokens;

  @Value("${gigachat.temperature:0.3}")
  private float temperature;

  @Value("${gigachat.top-p:0.9}")
  private float topP;

  @Value("${gigachat.repetition-penalty:1.05}")
  private float repetitionPenalty;

  public GigaChatService(GigaChatClient gigaChatClient) {
    this.gigaChatClient = gigaChatClient;
  }

  public String sendMessage(String prompt) {
    return executeWithLock(() -> {
      log.debug("Отправка запроса в GigaChat, длина prompt: {}", prompt.length());

      CompletionResponse response = gigaChatClient.completions(CompletionRequest.builder()
        .model(ModelName.GIGA_CHAT)
        .message(ChatMessage.builder()
          .content(prompt)
          .role(ChatMessageRole.USER)
          .build())
        .maxTokens(maxTokens)
        .temperature(temperature)
        .topP(topP)
        .repetitionPenalty(repetitionPenalty)
        .build());

      String content = response.choices().get(0).message().content();
      log.info("📊 Длина ответа GigaChat: {}", content != null ? content.length() : 0);
      return content != null ? content : "";
    });
  }

  public String chat(String systemPrompt, List<String> context) {
    return executeWithLock(() -> {
      log.debug("Отправка запроса в GigaChat (Chat Mode), контекст из {} сообщений", context.size());

      List<ChatMessage> messages = new ArrayList<>();
      messages.add(ChatMessage.builder()
        .content(systemPrompt)
        .role(ChatMessageRole.SYSTEM)
        .build());

      for (String message : context) {
        if (message.startsWith("User:")) {
          messages.add(ChatMessage.builder()
            .content(message.substring(5).trim())
            .role(ChatMessageRole.USER)
            .build());
        } else if (message.startsWith("AI:")) {
          messages.add(ChatMessage.builder()
            .content(message.substring(3).trim())
            .role(ChatMessageRole.ASSISTANT)
            .build());
        } else {
          messages.add(ChatMessage.builder()
            .content(message)
            .role(ChatMessageRole.USER)
            .build());
        }
      }

      CompletionResponse response = gigaChatClient.completions(CompletionRequest.builder()
        .model(ModelName.GIGA_CHAT)
        .messages(messages)
        .maxTokens(maxTokens)
        .temperature(temperature)
        .topP(topP)
        .repetitionPenalty(repetitionPenalty)
        .build());

      String content = response.choices().get(0).message().content();
      log.info("📊 Длина ответа GigaChat (Chat): {}", content != null ? content.length() : 0);
      return content != null ? content : "";
    });
  }

  public String chat(String systemPrompt, String userMessage) {
    return chat(systemPrompt, List.of("User: " + userMessage));
  }

  public String summarize(List<String> history, String prompt) {
    StringBuilder fullPrompt = new StringBuilder();
    fullPrompt.append(prompt).append("\n\nИстория диалога:\n");
    for (String message : history) {
      fullPrompt.append(message).append("\n");
    }
    return sendMessage(fullPrompt.toString());
  }

  /**
   * Вспомогательный метод для управления очередью (Semaphore)
   */
  private String executeWithLock(GigaChatOperation operation) {
    boolean acquired = false;
    try {
      // Ждем доступа к API до 30 секунд
      acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
      if (!acquired) {
        log.warn("⏳ Очередь к GigaChat переполнена. Запрос отменен.");
        return "Ошибка: сервер перегружен. Попробуйте позже.";
      }

      // Выполняем саму операцию
      String result = operation.run();

      // КРИТИЧЕСКИ ВАЖНО: Пауза в 600мс ПОСЛЕ запроса,
      // чтобы API GigaChat "отдышало" перед следующим вызовом.
      Thread.sleep(600);

      return result;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("❌ Поток прерван во время ожидания AI");
      return "";
    } catch (Exception e) {
      log.error("❌ Критическая ошибка GigaChat: {}", e.getMessage());
      return "";
    } finally {
      if (acquired) {
        semaphore.release();
      }
    }
  }

  @FunctionalInterface
  private interface GigaChatOperation {
    String run() throws Exception;
  }
}