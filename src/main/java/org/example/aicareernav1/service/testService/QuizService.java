package org.example.aicareernav1.service.testService;

import chat.giga.client.GigaChatClient;
import chat.giga.model.ModelName;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.dataBaseQuestion.QuestionEntity;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.promptService.QuizPromptService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final GigaChatClient gigaChatClient;
    private final QuizPromptService quizPromptService;
    private final ObjectMapper objectMapper;

    public List<QuestionDto> generateAndSaveQuestions(Long userId, String vacancyNow) {
        String userGrade = extractGrade(vacancyNow.toLowerCase());
        String profession = extractProfession(vacancyNow.toLowerCase(), userGrade);

        // 1. Сначала ищем в БД
        List<QuestionDto> finalQuestions = getQuestionsFromDb(profession, userGrade);

        // 2. Если в БД пусто — запускаем цикл запросов к нейронке (до 100 попыток)
        if (finalQuestions.isEmpty()) {
            log.info("🔍 В БД пусто для {}, запускаем цикл генерации через GigaChat...", profession);
            String prompt = quizPromptService.buildQuizPrompt(profession, userGrade);

            int maxRetries = 100;
            int attempt = 0;

            while (attempt < maxRetries && finalQuestions.isEmpty()) {
                attempt++;
                try {
                    log.info("🚀 Попытка AI №{} из {}", attempt, maxRetries);

                    CompletionResponse response = gigaChatClient.completions(CompletionRequest.builder()
                      .model(ModelName.GIGA_CHAT)
                      .message(ChatMessage.builder()
                        .content(prompt)
                        .role(ChatMessageRole.USER)
                        .build())
                      .build());

                    String content = response.choices().get(0).message().content();

                    // Логируем сырой ответ, если парсинг не удался (для отладки)
                    finalQuestions = quizPromptService.parseQuizResponse(content);

                    if (finalQuestions.isEmpty()) {
                        log.warn("⚠️ Попытка №{} вернула пустой результат (ошибка парсинга JSON)", attempt);
                        // Небольшая пауза, чтобы не спамить
                        Thread.sleep(150);
                    }
                } catch (Exception e) {
                    log.error("❌ Ошибка на попытке №{}: {}", attempt, e.getMessage());
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }
        }

        // Если даже после 100 попыток пусто — бросаем исключение
        if (finalQuestions.isEmpty()) {
            log.error("🛑 Не удалось получить вопросы после 100 попыток генерации.");
            throw new RuntimeException("Нейросеть временно не может сформировать корректный тест. Попробуйте позже.");
        }

        // 3. Сохранение в Redis
        String redisKey = "quiz:user:" + userId;
        redisTemplate.opsForValue().set(redisKey, finalQuestions, 1, TimeUnit.HOURS);

        return finalQuestions;
    }

    public void createQuizSession(Long userId) {
        UserEntity user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTestResult("{}");
        userRepository.save(user);
        log.info("Очищены старые ответы для пользователя {}", userId);
    }

    public List<QuestionDto> getQuestions(Long userId) {
        String redisKey = "quiz:user:" + userId;
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached == null) return Collections.emptyList();

        return objectMapper.convertValue(cached,
          objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDto.class));
    }

    public void saveAnswer(Long userId, String questionText, String answer) {
        UserEntity user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            Map<String, String> answersMap = new HashMap<>();
            if (user.getTestResult() != null && !user.getTestResult().isEmpty() && !user.getTestResult().equals("{}")) {
                answersMap = objectMapper.readValue(user.getTestResult(), Map.class);
            }
            answersMap.put(questionText, answer);
            user.setTestResult(objectMapper.writeValueAsString(answersMap));
            userRepository.save(user);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сохранения ответа", e);
        }
    }

    public Map<String, String> getAllAnswers(Long userId) {
        UserEntity user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            if (user.getTestResult() == null || user.getTestResult().isEmpty()) return Collections.emptyMap();
            return objectMapper.readValue(user.getTestResult(), Map.class);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private List<QuestionDto> getQuestionsFromDb(String tag, String userGrade) {
        var juniors = questionRepository.findAllByTagNameAndDifficulty(tag, "Junior");
        var middles = questionRepository.findAllByTagNameAndDifficulty(tag, "Middle");
        var seniors = questionRepository.findAllByTagNameAndDifficulty(tag, "Senior");

        if (juniors.isEmpty() && middles.isEmpty() && seniors.isEmpty()) return Collections.emptyList();

        List<QuestionDto> pool = new ArrayList<>();
        switch (userGrade) {
            case "senior" -> { pool.addAll(pickRandom(juniors, 4)); pool.addAll(pickRandom(middles, 6)); pool.addAll(pickRandom(seniors, 2)); }
            case "middle" -> { pool.addAll(pickRandom(middles, 6)); pool.addAll(pickRandom(juniors, 6)); }
            default -> { pool.addAll(pickRandom(juniors, 8)); pool.addAll(pickRandom(middles, 4)); }
        }
        Collections.shuffle(pool);
        for (int i = 0; i < pool.size(); i++) pool.get(i).setNumber(i + 1);
        return pool;
    }

    private List<QuestionDto> pickRandom(List<QuestionEntity> list, int count) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        List<QuestionEntity> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return copy.stream().limit(count).map(q -> {
            QuestionDto dto = new QuestionDto();
            dto.setQuestion(q.getText());
            return dto;
        }).collect(Collectors.toList());
    }

    private String extractGrade(String cleaned) {
        if (cleaned.contains("senior")) return "senior";
        if (cleaned.contains("middle")) return "middle";
        return "junior";
    }

    private String extractProfession(String cleaned, String grade) {
        return cleaned.replace(grade, "").trim();
    }
}