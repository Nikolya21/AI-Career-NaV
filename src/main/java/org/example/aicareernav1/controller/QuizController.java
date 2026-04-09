package org.example.aicareernav1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.promptService.QuizAnalysisPromptService;
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

        // 1. Сохраняем текущий ответ
        quizService.saveAnswer(userId, questionText, answer);

        List<QuestionDto> questions = quizService.getQuestions(userId);
        int currentNumber = questions.stream()
          .filter(q -> q.getQuestion().equals(questionText))
          .findFirst()
          .map(QuestionDto::getNumber)
          .orElse(0);

        // Ищем следующий вопрос
        var nextQuestion = questions.stream()
          .filter(q -> q.getNumber() == currentNumber + 1)
          .findFirst();

        if (nextQuestion.isPresent()) {
            return ResponseEntity.ok(nextQuestion.get());
        } else {
            // 2. ВОПРОСОВ БОЛЬШЕ НЕТ -> ЗАПУСКАЕМ АНАЛИЗ ПРЯМО ЗДЕСЬ
            log.info("🏁 Последний вопрос отвечен. Автоматический запуск анализа для ID: {}", userId);

            // Запускаем анализ (метод в Service у нас уже готов и делает saveAndFlush)
            quizService.runFullQuizAnalysis(userId);

            return ResponseEntity.noContent().build(); // Возвращаем 204 фронту
        }
    }

    @GetMapping("/answers/{userId}")
    public ResponseEntity<?> getAllAnswers(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getAllAnswers(userId));
    }
    @PostMapping("/analyze/{userId}")
    public ResponseEntity<String> analyze(@PathVariable Long userId) {
        log.info("📥 Получен запрос на AI-анализ для пользователя: {}", userId);

        // Вызываем метод из QuizService, который мы подготовили ранее
        String analysisResult = quizService.runFullQuizAnalysis(userId);

        log.info("✅ Анализ завершен и сохранен в БД для пользователя: {}", userId);
        return ResponseEntity.ok(analysisResult);
    }
}