package org.example.aicareernav1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.analysis.AnswerAnalysisRequest;
import org.example.aicareernav1.service.yandexGpt.YandexGptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AnalysisController {

  private final YandexGptService gptService;
  private final ObjectMapper objectMapper;

  // Вопросы должны совпадать с фронтендом
  private static final List<String> QUESTIONS = List.of(
      "Расскажите о вашем опыте работы с Java. Какие проекты вы делали?",
      "Что такое SOLID принципы? Приведите пример использования одного из них.",
      "Как бы вы объяснили не техническому человеку, что такое REST API?"
  );

  @PostMapping("/api/analyze-answers")
  public ResponseEntity<Map<String, Object>> analyzeAnswers(@RequestBody AnswerAnalysisRequest request) {
    Long userId = request.getUserId();
    List<AnswerAnalysisRequest.AnswerItem> answers = request.getAnswers();

    StringBuilder prompt = new StringBuilder();
    prompt.append("Ты — опытный технический HR. Оцени ответы кандидата на следующие вопросы. ")
        .append("Основной фактор (90% веса) — правильность и полнота ответов. ")
        .append("Дополнительные факторы (10% веса) — эмоции из голоса и поведение (моргания, касания лица). ")
        .append("Учитывай, что повышенное моргание и частые касания лица могут указывать на стресс. ")
        .append("Дай оценку в формате JSON:\n")
        .append("{\n")
        .append("  \"probability\": число от 0 до 100 (общая вероятность успеха),\n")
        .append("  \"behavior_impact\": число от 0 до 100 (насколько эмоции/поведение повлияли на результат, где 0 — не повлияли, 100 — сильно ухудшили),\n")
        .append("  \"recommendations\": \"текст рекомендаций по улучшению поведения и снижению стресса\"\n")
        .append("}\n\n");

    for (int i = 0; i < QUESTIONS.size(); i++) {
      String question = QUESTIONS.get(i);
      AnswerAnalysisRequest.AnswerItem answerItem = (i < answers.size()) ? answers.get(i) : null;
      String answerText = (answerItem != null && answerItem.getText() != null) ? answerItem.getText() : "Нет ответа";

      prompt.append("Вопрос ").append(i+1).append(": ").append(question).append("\n");
      prompt.append("Ответ кандидата: ").append(answerText).append("\n");

      if (answerItem != null) {
        // Эмоции
        if (answerItem.getEmotions() != null && !answerItem.getEmotions().isEmpty()) {
          prompt.append("Эмоции в голосе: ");
          answerItem.getEmotions().forEach((emotion, prob) ->
              prompt.append(emotion).append("=").append(String.format("%.2f", prob)).append("; "));
          prompt.append("\n");
        }
        prompt.append("Количество морганий за время ответа: ").append(answerItem.getBlinks()).append("\n");
        prompt.append("Время в секундах в течении которого пользователь касался лица(): ").append(answerItem.getFaceTouches()).append("\n");
      }
      prompt.append("\n");
    }

    prompt.append("На основе содержания ответов и поведения вычисли вероятность успеха, влияние поведения и дай рекомендации.\n");

    try {
      String gptResponse = gptService.sendMessage(prompt.toString());
      log.info("Ответ YandexGPT: {}", gptResponse);

      // Очистка от Markdown-обёртки
      String cleaned = gptResponse.trim();
      if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
      else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
      if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
      cleaned = cleaned.trim();

      JsonNode jsonNode = objectMapper.readTree(cleaned);
      int probability = jsonNode.has("probability") ? jsonNode.get("probability").asInt() : 50;
      int behaviorImpact = jsonNode.has("behavior_impact") ? jsonNode.get("behavior_impact").asInt() : 0;
      String recommendations = jsonNode.has("recommendations") ? jsonNode.get("recommendations").asText() : "Нет рекомендаций.";

      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("probability", probability);
      result.put("behavior_impact", behaviorImpact);
      result.put("recommendations", recommendations);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Ошибка при анализе ответов через YandexGPT", e);
      Map<String, Object> errorResult = new HashMap<>();
      errorResult.put("success", false);
      errorResult.put("error", "Не удалось получить оценку: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResult);
    }
  }
}