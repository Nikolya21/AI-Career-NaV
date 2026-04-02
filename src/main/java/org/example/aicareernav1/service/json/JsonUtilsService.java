package org.example.aicareernav1.service.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtilsService {

  private final ObjectMapper objectMapper;

  public String cleanJsonResponse(String rawResponse) {
    if (rawResponse == null || rawResponse.isBlank()) return "{}";
    try {
      int firstBrace = rawResponse.indexOf('{');
      int lastBrace = rawResponse.lastIndexOf('}');
      if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
        return rawResponse.substring(firstBrace, lastBrace + 1)
          .replace("```json", "")
          .replace("```", "")
          .trim();
      }
    } catch (Exception e) {
      log.warn("Ошибка очистки JSON: {}", e.getMessage());
    }
    return rawResponse.trim();
  }

  /**
   * Универсальный метод: очищает строку и парсит её в объект указанного класса.
   *
   * @param rawResponse сырой ответ от нейросети
   * @param clazz       класс, в который нужно преобразовать JSON
   * @return объект типа T или null в случае критической ошибки
   */
  public <T> T parseObject(String rawResponse, Class<T> clazz) {
    String cleaned = cleanJsonResponse(rawResponse);
    try {
      return objectMapper.readValue(cleaned, clazz);
    } catch (Exception e) {
      log.error("Ошибка при десериализации JSON в класс {}: {}", clazz.getSimpleName(), e.getMessage());
      try {
        // Пытаемся вернуть пустой объект, чтобы вызывающий код не упал по NullPointerException
        return clazz.getDeclaredConstructor().newInstance();
      } catch (Exception ex) {
        log.error("Не удалось создать пустой экземпляр класса {}", clazz.getSimpleName());
        return null;
      }
    }
  }

  public JsonNode parseTree(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (Exception e) {
      log.error("Ошибка парсинга JSON в JsonNode: {}", e.getMessage());
      return objectMapper.createObjectNode();
    }
  }

  public boolean isValidJson(String json) {
    try {
      objectMapper.readTree(json);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}