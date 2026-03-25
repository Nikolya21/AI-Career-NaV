package org.example.aicareernav1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.testService.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {
  private final QuizService quizService;
  private final UserRepository userRepository;

  /**
   * Генерирует тест для пользователя
   */
  @PostMapping("/generate/{userId}")
  public ResponseEntity<List<QuestionDto>> generateTest(@PathVariable Long userId) throws JsonProcessingException {
    String email = userRepository.findById(userId)
      .map(UserEntity::getEmail)
      .orElseThrow(() -> new RuntimeException("User not found"));

    List<QuestionDto> questions = quizService.generateAndSaveQuestions(userId, email);

    // Создаем сессию для хранения ответов
    quizService.createQuizSession(userId);

    return ResponseEntity.ok(questions);
  }

  /**
   * Получить первый вопрос
   */
  @GetMapping("/start/{userId}")
  public ResponseEntity<?> startQuiz(@PathVariable Long userId) {
    List<QuestionDto> questions = quizService.getQuestions(userId);
    if (questions == null || questions.isEmpty()) {
      return ResponseEntity.badRequest().body("Тест не найден. Сначала сгенерируйте вопросы.");
    }

    QuestionDto firstQuestion = questions.stream()
      .filter(q -> q.getNumber() == 1)
      .findFirst()
      .orElse(questions.get(0));

    return ResponseEntity.ok(firstQuestion);
  }

  /**
   * Сохранить ответ и получить следующий вопрос
   */
  @PostMapping("/answer/{userId}")
  public ResponseEntity<?> saveAnswer(
    @PathVariable Long userId,
    @RequestBody Map<String, String> request) {

    String questionText = request.get("question");
    String answer = request.get("answer");

    // 1. Просто сохраняем ответ
    quizService.saveAnswer(userId, questionText, answer);

    // 2. Получаем список вопросов
    List<QuestionDto> questions = quizService.getQuestions(userId);

    // Если списка нет вообще, просто возвращаем 204 (конец)
    if (questions == null || questions.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    // 3. Ищем текущий номер
    int currentNumber = questions.stream()
      .filter(q -> q.getQuestion().equals(questionText))
      .findFirst()
      .map(QuestionDto::getNumber)
      .orElse(0);

    // 4. Ищем следующий вопрос
    QuestionDto nextQuestion = questions.stream()
      .filter(q -> q.getNumber() == currentNumber + 1)
      .findFirst()
      .orElse(null);

    // 5. Если следующего нет — статус 204 (No Content)
    if (nextQuestion == null) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(nextQuestion);
  }


  /**
   * Получить все ответы (для отладки)
   */
  @GetMapping("/answers/{userId}")
  public ResponseEntity<?> getAllAnswers(@PathVariable Long userId) {
    Map<String, String> answers = quizService.getAllAnswers(userId);
    return ResponseEntity.ok(answers);
  }
}
