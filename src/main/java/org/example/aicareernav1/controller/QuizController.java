package org.example.aicareernav1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.service.testService.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {
  private final QuizService quizService;

  @PostMapping("/test-save/{userId}")
  public ResponseEntity<String> testSave(@PathVariable Long userId) throws JsonProcessingException {
    String mockJson = "[" +
      "{\"number\": 1, \"question\": \"Что такое Spring Boot?\"}," +
      "{\"number\": 2, \"question\": \"Зачем нужен Redis?\"}," +
      "{\"number\": 3, \"question\": \"Как работает JSON?\"}" +
      "]";

    quizService.processAndSave(userId, mockJson);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body("Тестовый JSON успешно сохранен в Redis для пользователя " + userId);
  }

  @GetMapping("/questions/{userId}")
  public ResponseEntity<List<QuestionDto>> getQuestions(@PathVariable Long userId) {
    return ResponseEntity.ok(quizService.getQuestions(userId));
  }
}
