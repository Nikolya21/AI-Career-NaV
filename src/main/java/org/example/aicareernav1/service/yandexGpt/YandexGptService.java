package org.example.aicareernav1.service.yandexGpt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class YandexGptService {

    @Value("${yandex.gpt.api-key}")
    private String apiKey;

    @Value("${yandex.gpt.folder-id}")
    private String folderId;

    @Value("${yandex.gpt.url}")
    private String apiUrl;

    @Value("${yandex.gpt.max-tokens:2000}")
    private String maxTokens;

    @Value("${yandex.gpt.temperature:0.3}")
    private double temperature;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendMessage(String prompt) {
        return chat("Ты полезный помощник.", List.of("User: " + prompt));
    }

    public String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, List.of("User: " + userMessage));
    }

    public String chat(String systemPrompt, List<String> context) {
        log.debug("Запрос в YandexGPT, контекст: {} сообщений", context.size());

        // 1. Формируем список сообщений для Yandex
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "text", systemPrompt));

        for (String msg : context) {
            if (msg.startsWith("User:")) {
                messages.add(Map.of("role", "user", "text", msg.substring(5).trim()));
            } else if (msg.startsWith("AI:")) {
                messages.add(Map.of("role", "assistant", "text", msg.substring(3).trim()));
            } else {
                messages.add(Map.of("role", "user", "text", msg));
            }
        }

        // 2. Тело запроса
        Map<String, Object> body = new HashMap<>();
        body.put("modelUri", "gpt://" + folderId + "/yandexgpt/latest"); // или /yandexgpt-pro/5.1
        body.put("completionOptions", Map.of(
                "stream", false,
                "temperature", temperature,
                "maxTokens", maxTokens
        ));
        body.put("messages", messages);

        // 3. Заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Api-Key " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // 4. Парсинг ответа (аналог response.choices().get(0)...)
            Map result = (Map) response.getBody().get("result");
            List alternatives = (List) result.get("alternatives");
            Map message = (Map) ((Map) alternatives.get(0)).get("message");

            String content = (String) message.get("text");
            log.info("📊 Ответ YandexGPT получен, длина: {}", content.length());
            return content;
        } catch (Exception e) {
            log.error("Ошибка YandexGPT API: {}", e.getMessage());
            return "Ошибка при получении ответа";
        }
    }

    public String summarize(List<String> history, String prompt) {
        StringBuilder fullPrompt = new StringBuilder(prompt).append("\n\nИстория:\n");
        history.forEach(m -> fullPrompt.append(m).append("\n"));
        return sendMessage(fullPrompt.toString());
    }
}
