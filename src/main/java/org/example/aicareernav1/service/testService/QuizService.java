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
  public void processAndSave(Long userId, String gigaChatResponse) throws JsonProcessingException {
    List<QuestionDto> questions = objectMapper.readValue(gigaChatResponse,
      new TypeReference<List<QuestionDto>>() {});
    saveQuestions(userId, questions);
  }

  public void saveQuestions(Long userId, List<QuestionDto> questions) {
    String key = "user_quiz:" + userId;
    redisTemplate.opsForValue().set(key, questions, Duration.ofMinutes(30));
  }

  public List<QuestionDto> getQuestions(Long userId) {
    String key = "user_quiz:" + userId;
    return (List<QuestionDto>) redisTemplate.opsForValue().get(key);
  }
}