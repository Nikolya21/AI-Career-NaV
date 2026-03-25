package org.example.aicareernav1.service.testService;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.promptService.ResultPrompt;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
  private final GigaChatService gigaChatService;
  private final ResultPrompt resultPrompt;
  private final ObjectMapper objectMapper;

  /**
   * Анализирует ответы кандидата и возвращает текст для сохранения
   * @param testResults Map<Вопрос, Ответ>
   * @return Текст с анализом для сохранения в БД
   */
  public String analyzeAndGetText(Map<String, String> testResults) {
    log.info("Начинаем анализ ответов кандидата. Количество ответов: {}", testResults.size());

    // 1. Формируем промпт
    String prompt = resultPrompt.buildAnalysisPrompt(testResults);

    // 2. Отправляем в нейросеть
    String rawResponse = gigaChatService.sendMessage(prompt);

    // 3. Возвращаем результат (можно сохранить как есть или отформатировать)
    return rawResponse;
  }

  /**
   * Анализирует с повторными попытками
   */
  public String analyzeWithRetry(Map<String, String> testResults, int attempt) {
    log.info("Попытка анализа #{}, количество ответов: {}", attempt, testResults.size());

    try {
      return analyzeAndGetText(testResults);
    } catch (Exception e) {
      log.error("Ошибка на попытке {}: {}", attempt, e.getMessage());

      if (attempt < 3) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
        return analyzeWithRetry(testResults, attempt + 1);
      } else {
        throw new RuntimeException("Не удалось проанализировать ответы после 3 попыток", e);
      }
    }
  }

  /**
   * Форматирует ответ для красивого сохранения (опционально)
   */
  public String formatAnalysis(String analysis) {
    // Можно убрать JSON обертки, если они есть
    if (analysis.contains("```json")) {
      analysis = analysis.replaceAll("```json\\s*", "")
        .replaceAll("```\\s*", "")
        .trim();
    }
    return analysis;
  }
}
