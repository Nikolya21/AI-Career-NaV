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

@Slf4j
@Service
public class GigaChatService {

  private final GigaChatClient gigaChatClient;

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
  }

  /**
   * Чат с системным промптом и контекстом (для диалогов)
   * @param systemPrompt системный промпт (роль/инструкция для модели)
   * @param context список сообщений из истории диалога
   * @return ответ от модели
   */
  public String chat(String systemPrompt, List<String> context) {
    log.debug("Отправка запроса в GigaChat, systemPrompt длина: {}, контекст из {} сообщений",
      systemPrompt.length(), context.size());

    // Формируем список сообщений
    List<ChatMessage> messages = new ArrayList<>();

    // Добавляем системный промпт
    messages.add(ChatMessage.builder()
      .content(systemPrompt)
      .role(ChatMessageRole.SYSTEM)
      .build());

    // Добавляем контекст диалога
    for (String message : context) {
      // Определяем роль по префиксу (User: или AI:)
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
        // Если префикса нет, считаем сообщением пользователя
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
    log.info("📊 Длина ответа GigaChat: {}", content != null ? content.length() : 0);
    return content != null ? content : "";
  }

  /**
   * Чат с системным промптом и одним сообщением (упрощенный вариант)
   */
  public String chat(String systemPrompt, String userMessage) {
    List<String> context = List.of("User: " + userMessage);
    return chat(systemPrompt, context);
  }

  /**
   * Создать резюме диалога
   * @param history полная история диалога
   * @param prompt промпт для создания резюме
   * @return резюме
   */
  public String summarize(List<String> history, String prompt) {
    log.debug("Создание резюме диалога из {} сообщений", history.size());

    // Формируем полный запрос
    StringBuilder fullPrompt = new StringBuilder();
    fullPrompt.append(prompt).append("\n\n");
    fullPrompt.append("История диалога:\n");

    for (String message : history) {
      fullPrompt.append(message).append("\n");
    }

    return sendMessage(fullPrompt.toString());
  }
}
