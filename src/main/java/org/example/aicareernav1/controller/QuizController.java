package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.RoadmapGenerationRequest;
import org.example.aicareernav1.dto.roadmap.response.RoadmapResponse;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.example.aicareernav1.service.testService.QuizService;
import org.example.aicareernav1.service.user.impl.UserServiceImpl;
import org.example.aicareernav1.service.email.AnalysisMailService; // Добавь этот импорт!

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
    private final UserServiceImpl userService;
    private final RoadmapService roadmapService;
    private final AnalysisMailService mailService; // 1. Добавили сервис почты

    @PostMapping("/generate/{userId}")
    public ResponseEntity<List<QuestionDto>> generateTest(@PathVariable Long userId) {
        UserEntity user = userService.getUserById(userId);
        String vacancy = user.getVacancyNow();
        quizService.createQuizSession(userId);
        List<QuestionDto> questions = quizService.generateAndSaveQuestions(userId, vacancy);
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

        var nextQuestion = questions.stream()
          .filter(q -> q.getNumber() == currentNumber + 1)
          .findFirst();

        if (nextQuestion.isPresent()) {
            return ResponseEntity.ok(nextQuestion.get());
        } else {
            log.info("🏁 Тест завершен для пользователя {}. Запуск AI-анализа.", userId);
            quizService.runFullQuizAnalysis(userId);
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/answers/{userId}")
    public ResponseEntity<?> getAllAnswers(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getAllAnswers(userId));
    }

    @PostMapping("/{userId}/finalize-and-generate")
    public ResponseEntity<RoadmapResponse> finalize(@PathVariable Long userId) {
        log.info("🚀 Финализация теста и генерация Roadmap для пользователя: {}", userId);

        // 1. Получаем пользователя и результаты его анализа
        UserEntity user = userService.getUserById(userId);
        String analysisResult = quizService.runFullQuizAnalysis(userId);

        // 2. Формируем запрос на генерацию Roadmap
        RoadmapGenerationRequest request = new RoadmapGenerationRequest();
        request.setUserId(userId);
        request.setTestResult(analysisResult);
        request.setJobTitle(user.getVacancyNow());
        request.setRequirements(user.getVacancyRequirements());

        // 3. Генерируем Roadmap через RoadmapService
        RoadmapResponse roadmapResponse = roadmapService.generateFullRoadmap(request);

        // 4. Сохраняем ID созданной дорожной карты в сущности пользователя
        user.setRoadmapId(roadmapResponse.getId());
        userRepository.save(user);

        log.info("UserEntity сохранен в БД: {}", user.getId());

        // 5. ОТПРАВКА НА ПОЧТУ
        try {
            log.info("📧 Попытка отправки анализа на почту: {}", user.getEmail());
            mailService.sendAnalysisByEmail(user.getEmail());
            log.info("✅ Письмо успешно поставлено в очередь отправки");
        } catch (Exception e) {
            log.error("❌ Ошибка при отправке письма: {}", e.getMessage());
            // Мы не возвращаем ошибку клиенту, чтобы он всё равно получил Roadmap в браузере
        }

        log.info("✅ Roadmap успешно создан с ID: {} и привязан к пользователю: {}", roadmapResponse.getId(), userId);

        return ResponseEntity.ok(roadmapResponse);
    }
}