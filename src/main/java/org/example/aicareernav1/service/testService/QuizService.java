package org.example.aicareernav1.service.testService;

import chat.giga.client.GigaChatClient;
import chat.giga.model.ModelName;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.dataBaseQuestion.QuestionEntity;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.promptService.QuizAnalysisPromptService;
import org.example.aicareernav1.service.promptService.QuizPromptService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final QuizAnalysisPromptService quizAnalysisPromptService;

    public List<QuestionDto> generateAndSaveQuestions(Long userId, String vacancyNow) {
        String userGrade = extractGrade(vacancyNow.toLowerCase());
        String profession = extractProfession(vacancyNow.toLowerCase(), userGrade);

        // 1. Сбор вопросов (БД или AI)
        List<QuestionDto> finalQuestions = getQuestionsFromDb(profession, userGrade);

        if (finalQuestions.isEmpty()) {
            finalQuestions = generateAiQuestions(profession, userGrade);
        }

        // 2. ОПРЕДЕЛЕНИЕ НЕОБХОДИМОСТИ КОМПИЛЯТОРА (Наш новый шаг)
        enrichQuestionsWithCompilerFlag(finalQuestions);

        // 3. Сохранение в Redis
        String redisKey = "quiz:user:" + userId;
        redisTemplate.opsForValue().set(redisKey, finalQuestions, 1, TimeUnit.HOURS);

        return finalQuestions;
    }

    private void enrichQuestionsWithCompilerFlag(List<QuestionDto> questions) {
        try {
            log.info("🔍 Запрос к AI для определения необходимости компилятора для 12 вопросов...");
            String questionsJson = objectMapper.writeValueAsString(questions);
            String prompt = quizPromptService.buildCompilerCheckPrompt(questionsJson);

            CompletionResponse response = gigaChatClient.completions(CompletionRequest.builder()
              .model(ModelName.GIGA_CHAT)
              .message(ChatMessage.builder()
                .content(prompt)
                .role(ChatMessageRole.USER)
                .build())
              .build());

            String content = response.choices().get(0).message().content();

            // Парсим ответ [true, false, ...]
            int start = content.indexOf("[");
            int end = content.lastIndexOf("]") + 1;
            if (start != -1 && end != 0) {
                String jsonArray = content.substring(start, end).toLowerCase()
                  .replace("да", "true").replace("нет", "false"); // На всякий случай, если AI ответит по-русски

                List<Boolean> flags = objectMapper.readValue(jsonArray, new TypeReference<List<Boolean>>() {});

                log.info("🎯 Ответы AI по компилятору: {}", flags);

                for (int i = 0; i < questions.size() && i < flags.size(); i++) {
                    questions.get(i).setCompilerRequired(flags.get(i));
                }
                log.info("✅ Флаги компилятора успешно расставлены");
            }
        } catch (Exception e) {
            log.error("❌ Не удалось определить необходимость компилятора: {}", e.getMessage());
            // По умолчанию оставляем false (уже задано в DTO)
        }
    }

    private List<QuestionDto> generateAiQuestions(String profession, String userGrade) {
        String prompt = quizPromptService.buildQuizPrompt(profession, userGrade);
        List<QuestionDto> questions = new ArrayList<>();
        int attempt = 0;

        while (attempt < 35 && questions.isEmpty()) {
            attempt++;
            try {
                CompletionResponse response = gigaChatClient.completions(CompletionRequest.builder()
                  .model(ModelName.GIGA_CHAT)
                  .message(ChatMessage.builder().content(prompt).role(ChatMessageRole.USER).build())
                  .build());
                questions = quizPromptService.parseQuizResponse(response.choices().get(0).message().content());
                if (questions.isEmpty()) Thread.sleep(150);
            } catch (Exception e) {
                log.warn("Attempt {} failed", attempt);
            }
        }
        return questions;
    }

    public void createQuizSession(Long userId) {
        UserEntity user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTestResult("{}");
        user.setTestAnalysis(null);
        userRepository.save(user);
    }

    public List<QuestionDto> getQuestions(Long userId) {
        String redisKey = "quiz:user:" + userId;
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached == null) return Collections.emptyList();

        return objectMapper.convertValue(cached,
          objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDto.class));
    }

    @Transactional
    public void saveAnswer(Long userId, String questionText, String answer) {
        UserEntity user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            Map<String, String> answersMap;
            String currentResult = user.getTestResult();
            if (currentResult == null || currentResult.isEmpty() || currentResult.equals("{}")) {
                answersMap = new HashMap<>();
            } else {
                answersMap = objectMapper.readValue(currentResult, new TypeReference<HashMap<String, String>>() {});
            }
            answersMap.put(questionText, answer);
            user.setTestResult(objectMapper.writeValueAsString(answersMap));
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Save answer failed");
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

        log.info("📊 Поиск для тега [{}]: Найдено Jun: {}, Mid: {}, Sen: {}",
          tag, juniors.size(), middles.size(), seniors.size());

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

    @Transactional
    public String runFullQuizAnalysis(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        String vacancy = user.getVacancyNow();
        String answersJson = user.getTestResult();
        if (answersJson == null || answersJson.equals("{}")) return "Нет ответов.";

        String prompt = quizAnalysisPromptService.buildAnalysisPrompt(vacancy, answersJson);
        try {
            CompletionResponse response = gigaChatClient.completions(CompletionRequest.builder()
              .model(ModelName.GIGA_CHAT)
              .message(ChatMessage.builder().content(prompt).role(ChatMessageRole.USER).build())
              .build());
            String result = response.choices().get(0).message().content();
            user.setTestAnalysis(result);
            userRepository.saveAndFlush(user);
            return result;
        } catch (Exception e) {
            return "Ошибка анализа.";
        }
    }
}