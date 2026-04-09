package org.example.aicareernav1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AnalysisController {

  private final GigaChatService gigaChatService;
  private final ObjectMapper objectMapper;

  // Вопросы, которые задавались (должны совпадать с вопросами на фронте)
  private static final List<String> QUESTIONS = Arrays.asList(
      "Расскажите о вашем опыте работы с Java. Какие проекты вы делали?",
      "Что такое SOLID принципы? Приведите пример использования одного из них.",
      "Как бы вы объяснили не техническому человеку, что такое REST API?"
  );

  @PostMapping("/api/analyze-answers")
  public ResponseEntity<Map<String, Object>> analyzeAnswers(@RequestBody Map<String, String> payload) {
    Long userId = Long.parseLong(payload.get("userId"));
    String answersText = payload.get("answers");

    String[] answerArray = answersText.split("\n\n");
    List<String> answers = Arrays.asList(answerArray);

    StringBuilder prompt = new StringBuilder();
    prompt.append("Ты — опытный технический HR. Оцени ответы кандидата на следующие вопросы. ")
        .append("Дай вероятность (в процентах) того, что кандидат успешно пройдёт собеседование на позицию Java-разработчика. ")
        .append("Также добавь краткий комментарий по каждому ответу. Ответ строго в формате JSON:\n")
        .append("{\"probability\": число от 0 до 100, \"comments\": \"текст комментария\"}\n\n");

    for (int i = 0; i < QUESTIONS.size(); i++) {
      String answer = (i < answers.size()) ? answers.get(i) : "Нет ответа";
      prompt.append("Вопрос ").append(i+1).append(": ").append(QUESTIONS.get(i)).append("\n");
      prompt.append("Ответ кандидата: ").append(answer).append("\n\n");
    }

    try {
      String gigaResponse = gigaChatService.sendMessage(prompt.toString());
      log.info("Ответ GigaChat: {}", gigaResponse);

      // Очистка от Markdown-обёртки
      String cleaned = gigaResponse.trim();
      if (cleaned.startsWith("```json")) {
        cleaned = cleaned.substring(7);
      } else if (cleaned.startsWith("```")) {
        cleaned = cleaned.substring(3);
      }
      if (cleaned.endsWith("```")) {
        cleaned = cleaned.substring(0, cleaned.length() - 3);
      }
      cleaned = cleaned.trim();

      JsonNode jsonNode = objectMapper.readTree(cleaned);
      int probability = jsonNode.get("probability").asInt();
      String comments = jsonNode.get("comments").asText();

      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("probability", probability);
      result.put("comments", comments);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Ошибка при анализе ответов через GigaChat", e);
      Map<String, Object> errorResult = new HashMap<>();
      errorResult.put("success", false);
      errorResult.put("error", "Не удалось получить оценку от GigaChat: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResult);
    }
  }
}