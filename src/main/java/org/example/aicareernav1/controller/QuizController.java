package org.example.aicareernav1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.testService.QuizService;
import org.example.aicareernav1.service.userService.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/generate/{userId}")
    public ResponseEntity<List<QuestionDto>> generateTest(@PathVariable Long userId) throws JsonProcessingException {
        String email = userRepository.findById(userId)
          .map(UserEntity::getEmail)
          .orElseThrow(() -> new RuntimeException("User not found"));

        String vacancy = userService.getVacancyByEmail(email);
        List<QuestionDto> questions = quizService.generateAndSaveQuestions(userId, vacancy);

        quizService.createQuizSession(userId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/start/{userId}")
    public ResponseEntity<?> startQuiz(@PathVariable Long userId) {
        List<QuestionDto> questions = quizService.getQuestions(userId);
        if (questions.isEmpty()) {
            return ResponseEntity.badRequest().body("Тест не найден. Сначала сгенерируйте вопросы.");
        }
        return ResponseEntity.ok(questions.get(0));
    }

    @PostMapping("/answer/{userId}")
    public ResponseEntity<?> saveAnswer(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String questionText = request.get("question");
        String answer = request.get("answer");

        quizService.saveAnswer(userId, questionText, answer);

        List<QuestionDto> questions = quizService.getQuestions(userId);
        int currentNumber = questions.stream()
          .filter(q -> q.getQuestion().equals(questionText))
          .findFirst()
          .map(QuestionDto::getNumber)
          .orElse(0);

        return questions.stream()
          .filter(q -> q.getNumber() == currentNumber + 1)
          .findFirst()
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/answers/{userId}")
    public ResponseEntity<?> getAllAnswers(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getAllAnswers(userId));
    }
}