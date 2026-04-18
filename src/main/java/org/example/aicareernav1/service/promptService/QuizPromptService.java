package org.example.aicareernav1.service.promptService;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class QuizPromptService {

  private final ObjectMapper objectMapper;

  /**
   * Формирует промпт согласно твоим требованиям по распределению уровней
   */
  public String buildQuizPrompt(String profession, String grade) {
    return String.format(
      "Ты — технический интервьюер. Составь тест из 12 открытых вопросов для вакансии '%s'. " +
        "Учитывай, что кандидат претендует на уровень %s. " +
        "Сгенерируй вопросы строго в следующих пропорциях: " +
        "1. Если грейд junior: 8 простых (базовый синтаксис, основы) и 4 средних (практика, паттерны). " +
        "2. Если грейд middle: 6 средних и 6 простых. " +
        "3. Если грейд senior: 4 простых, 6 средних и 2 сложных (архитектура, highload). " +
        "Верни ответ ТОЛЬКО в формате JSON массива объектов: [{\"number\": 1, \"question\": \"текст\"}]. " +
        "Никаких пояснений до или после JSON.",
      profession, grade
    );
  }

  public String buildCompilerCheckPrompt(String questionsJson) {
    return "Ты — эксперт по Java. Твоя задача: пометить вопросы, которые требуют написания кода. " +
      "КРИТЕРИЙ: Если вопрос просит 'Написать метод', 'Реализовать паттерн', 'Привести пример кода' или 'Написать класс' — ставь true. " +
      "Если вопрос теоретический ('Что такое...', 'В чем отличие...') — ставь false. " +
      "ВАЖНО: Ты должен вернуть ТОЛЬКО JSON массив из 12 элементов boolean. " +
      "Никаких пояснений, только [true, false, ...]. " +
      "ВОПРОСЫ: " + questionsJson;
  }

  public List<QuestionDto> parseQuizResponse(String content) {
    try {
      int start = content.indexOf("[");
      int end = content.lastIndexOf("]") + 1;
      if (start == -1 || end == 0) return Collections.emptyList();

      String json = content.substring(start, end);
      return objectMapper.readValue(json,
        objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDto.class));
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}