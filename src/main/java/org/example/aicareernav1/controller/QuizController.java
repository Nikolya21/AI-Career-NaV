package org.example.aicareernav1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.entity.userEntity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.promptService.TestPrompt;
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
  private final GigaChatService gigaChatService;
  private final TestPrompt testPrompt;
  private final UserRepository userRepository;

  @PostMapping("/generate-Test/{userId}")
  public ResponseEntity<List<QuestionDto>> generateAndSave(@PathVariable Long userId) throws JsonProcessingException {
    String email = userRepository.findById(userId).map(UserEntity::getEmail)
      .orElseThrow(() -> new RuntimeException("User not found"));
    String prompt = testPrompt.buildOpenTestPrompt(email);
    String gigaChatResponse = gigaChatService.sendMessage(prompt);
    List<QuestionDto> questions = quizService.processAndSave(userId, gigaChatResponse);
    return ResponseEntity.ok(questions);
  }
}
