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
}