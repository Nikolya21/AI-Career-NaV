package org.example.aicareernav1.service.testService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.promptService.TestPrompt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {
  private final ObjectMapper objectMapper;
  private final RedisTemplate<String, Object> redisTemplate;
  private final GigaChatService gigaChatService;
  private final TestPrompt testPrompt;

  /**
   * Генерирует и сохраняет вопросы для пользователя
   */

  public List<QuestionDto> generateAndSaveQuestions(Long userId, String email) throws JsonProcessingException {
    String prompt = testPrompt.buildOpenTestPrompt(email);
    List<QuestionDto> questions = generateWithRetry(prompt, 1);
    saveQuestions(userId, questions);
    return questions;
  }

  /**
   * Генерирует вопросы с повторными попытками при ошибке парсинга
   */
  public List<QuestionDto> generateWithRetry(String prompt, int attempt) throws JsonProcessingException {
    log.info("Попытка генерации вопросов #{}", attempt);

    // 1. Получаем сырой ответ от нейронки
    String rawResponse = gigaChatService.sendMessage(prompt);

    // 2. Извлекаем JSON из ответа
    String extractedJson = extractJson(rawResponse);

    // 3. Восстанавливаем поврежденный JSON (если нужно)
    String fixedJson = repairMalformedJson(extractedJson);

    try {
      // 4. Пробуем парсить
      return objectMapper.readValue(fixedJson, new TypeReference<List<QuestionDto>>() {});
    } catch (Exception e) {
      log.error("Ошибка парсинга на попытке {}: {}", attempt, e.getMessage());
      log.debug("Проблемный JSON: {}", fixedJson);

      if (attempt < 3) {
        // Рекурсия: пробуем еще раз
        return generateWithRetry(prompt, attempt + 1);
      } else {
        // Последняя попытка - пробуем ручной парсинг
        try {
          return manualParse(fixedJson);
        } catch (Exception ex) {
          throw new RuntimeException("AI не смог выдать валидный JSON после 3 попыток. Последний ответ: " + fixedJson, ex);
        }
      }
    }
  }

  /**
   * Сохраняет вопросы в Redis
   */
  public void saveQuestions(Long userId, List<QuestionDto> questions) {
    String key = "user_quiz:" + userId;
    redisTemplate.opsForValue().set(key, questions, Duration.ofMinutes(30));
  }

  /**
   * Получает вопросы из Redis
   */
  public List<QuestionDto> getQuestions(Long userId) {
    String key = "user_quiz:" + userId;
    Object data = redisTemplate.opsForValue().get(key);
    if (data == null) return null;
    return objectMapper.convertValue(data, new TypeReference<List<QuestionDto>>() {});
  }

  /**
   * Извлекает JSON из ответа AI
   */
  private String extractJson(String response) {
    if (response == null) return null;

    // Удаляем markdown обертки
    if (response.contains("```")) {
      String cleaned = response.replaceAll("(?s)```json\\s*", "")
        .replaceAll("(?s)```\\s*", "")
        .trim();
      return cleaned;
    }
    return response.trim();
  }

  /**
   * Восстанавливает поврежденный JSON
   */
  private String repairMalformedJson(String json) {
    if (json == null) return null;

    log.debug("Исходный JSON для восстановления: {}", json);

    String fixed = json;

    // 1. Удаляем лишние запятые
    fixed = fixed.replaceAll(",\\s*]", "]")
      .replaceAll(",\\s*}", "}");

    // 2. Исправляем незакрытые объекты
    fixed = fixed.replaceAll("\\}\\s*\\{", "},{");

    // 3. Добавляем недостающие закрывающие скобки
    fixed = balanceBrackets(fixed);

    // 4. Убеждаемся, что это массив
    if (!fixed.trim().startsWith("[")) {
      fixed = "[" + fixed;
    }
    if (!fixed.trim().endsWith("]")) {
      fixed = fixed + "]";
    }

    log.debug("Восстановленный JSON: {}", fixed);

    return fixed;
  }

  /**
   * Балансирует скобки в JSON
   */
  private String balanceBrackets(String json) {
    int openBraces = 0;
    int openBrackets = 0;

    for (char c : json.toCharArray()) {
      if (c == '{') openBraces++;
      else if (c == '}') openBraces--;
      else if (c == '[') openBrackets++;
      else if (c == ']') openBrackets--;
    }

    StringBuilder sb = new StringBuilder(json);
    while (openBraces > 0) {
      sb.append("}");
      openBraces--;
    }
    while (openBrackets > 0) {
      sb.append("]");
      openBrackets--;
    }

    return sb.toString();
  }

  /**
   * Ручной парсинг JSON, если обычный не сработал
   */
  private List<QuestionDto> manualParse(String json) throws JsonProcessingException {
    log.info("Пробуем ручной парсинг JSON");

    // Пробуем найти все объекты вопросов
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
      "\\{\\s*\"number\"\\s*:\\s*(\\d+)\\s*,\\s*\"question\"\\s*:\\s*\"([^\"]*)\"\\s*\\}"
    );
    java.util.regex.Matcher matcher = pattern.matcher(json);

    java.util.List<QuestionDto> questions = new java.util.ArrayList<>();
    while (matcher.find()) {
      QuestionDto dto = new QuestionDto();
      dto.setNumber(Integer.parseInt(matcher.group(1)));
      dto.setQuestion(matcher.group(2));
      questions.add(dto);
    }

    if (questions.isEmpty()) {
      throw new JsonProcessingException("Не удалось распарсить вопросы вручную") {};
    }

    return questions;
  }

  /**
   * Простая обработка без retry (для обратной совместимости)
   */
  public List<QuestionDto> processAndSave(Long userId, String gigaChatResponse) throws JsonProcessingException {
    String cleanJson = extractJson(gigaChatResponse);
    List<QuestionDto> questions = objectMapper.readValue(cleanJson,
      new TypeReference<List<QuestionDto>>() {});
    saveQuestions(userId, questions);
    return questions;
  }

  public void deleteQuestions(Long userId) {
    String key = "user_quiz:" + userId;
    redisTemplate.delete(key);
  }
}