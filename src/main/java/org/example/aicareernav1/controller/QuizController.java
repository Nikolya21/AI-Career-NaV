package org.example.aicareernav1.controller;

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
    public ResponseEntity<List<QuestionDto>> generateTest(@PathVariable Long userId) {
        // Получаем email пользователя
        String email = userRepository.findById(userId)
          .map(UserEntity::getEmail)
          .orElseThrow(() -> new RuntimeException("User not found"));

        // Получаем вакансию/тему
        String vacancy = userService.getVacancyByEmail(email);

        // Очищаем старую сессию (результаты и анализ) перед генерацией нового теста
        quizService.createQuizSession(userId);

        // Генерируем вопросы (БД + AI), проставляем флаги компилятора и сохраняем в Redis
        List<QuestionDto> questions = quizService.generateAndSaveQuestions(userId, vacancy);

        return ResponseEntity.ok(questions);
    }

    @GetMapping("/start/{userId}")
    public ResponseEntity<?> startQuiz(@PathVariable Long userId) {
        // Получаем список вопросов из Redis
        List<QuestionDto> questions = quizService.getQuestions(userId);

        if (questions.isEmpty()) {
            return ResponseEntity.badRequest().body("Тест не найден. Сначала сгенерируйте вопросы.");
        }

        // Возвращаем первый вопрос (number 1)
        return ResponseEntity.ok(questions.get(0));
    }

    @PostMapping("/answer/{userId}")
    public ResponseEntity<?> saveAnswer(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String questionText = request.get("question");
        String answer = request.get("answer");

        // 1. Сохраняем ответ в JSON-поле пользователя в БД
        quizService.saveAnswer(userId, questionText, answer);

        // 2. Получаем все вопросы из Redis, чтобы найти следующий
        List<QuestionDto> questions = quizService.getQuestions(userId);

        int currentNumber = questions.stream()
          .filter(q -> q.getQuestion().equals(questionText))
          .findFirst()
          .map(QuestionDto::getNumber)
          .orElse(0);

        // Ищем следующий вопрос по порядку
        var nextQuestion = questions.stream()
          .filter(q -> q.getNumber() == currentNumber + 1)
          .findFirst();

        if (nextQuestion.isPresent()) {
            return ResponseEntity.ok(nextQuestion.get());
        } else {
            // 3. Если вопросов больше нет — запускаем финальный анализ
            log.info("🏁 Тест завершен для пользователя {}. Запуск AI-анализа.", userId);
            quizService.runFullQuizAnalysis(userId);

            // Возвращаем 204 (No Content), чтобы фронт понял: тест окончен
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/answers/{userId}")
    public ResponseEntity<?> getAllAnswers(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getAllAnswers(userId));
    }

    @PostMapping("/analyze/{userId}")
    public ResponseEntity<String> analyze(@PathVariable Long userId) {
        log.info("📥 Ручной запрос на анализ для пользователя: {}", userId);
        String analysisResult = quizService.runFullQuizAnalysis(userId);
        return ResponseEntity.ok(analysisResult);
    }
}