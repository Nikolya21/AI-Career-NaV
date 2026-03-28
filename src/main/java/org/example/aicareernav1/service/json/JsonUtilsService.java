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