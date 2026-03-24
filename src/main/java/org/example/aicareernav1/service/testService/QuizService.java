package org.example.aicareernav1.service.testService;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
  private final ObjectMapper objectMapper;
  private final RedisTemplate<String, Object> redisTemplate;
  public List<QuestionDto> processAndSave(Long userId, String gigaChatResponse) throws JsonProcessingException {
    String cleanJson = extractJson(gigaChatResponse);
    List<QuestionDto> questions = objectMapper.readValue(cleanJson,
      new TypeReference<List<QuestionDto>>() {});
    saveQuestions(userId, questions);
    return questions;
  }

  public void saveQuestions(Long userId, List<QuestionDto> questions) {
    String key = "user_quiz:" + userId;
    redisTemplate.opsForValue().set(key, questions, Duration.ofMinutes(30));
  }

  public List<QuestionDto> getQuestions(Long userId) {
    String key = "user_quiz:" + userId;
    Object data = redisTemplate.opsForValue().get(key);
    if (data == null) return null;
    return objectMapper.convertValue(data, new TypeReference<List<QuestionDto>>() {});
  }


  private String extractJson(String response) {
    if (response.contains("```")) {
      return response.replaceAll("```json|```", "").trim();
    }
    return response.trim();
  }
}