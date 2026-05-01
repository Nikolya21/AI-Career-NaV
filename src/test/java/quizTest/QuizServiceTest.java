package quizTest;

import chat.giga.client.GigaChatClient;
import chat.giga.model.completion.CompletionResponse;
import chat.giga.model.completion.Choice;
import chat.giga.model.completion.ChoiceMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.promptService.QuizAnalysisPromptService;
import org.example.aicareernav1.service.promptService.QuizPromptService;
import org.example.aicareernav1.service.testService.QuizService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations; // Мок для операций Redis
    @Mock private QuestionRepository questionRepository;
    @Mock private UserRepository userRepository;
    @Mock private GigaChatClient gigaChatClient;
    @Mock private QuizPromptService quizPromptService;
    @Mock private QuizAnalysisPromptService quizAnalysisPromptService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private QuizService quizService;

    @Test
    void shouldEnrichQuestionsWithCompilerFlags() throws Exception {
        // Подготовка данных
        String aiResponseJson = "[true, false]";

        // 1. Настройка GigaChat (с использованием ChoiceMessage)
        ChoiceMessage mockChoiceMessage = mock(ChoiceMessage.class);
        when(mockChoiceMessage.content()).thenReturn(aiResponseJson);

        Choice mockChoice = mock(Choice.class);
        when(mockChoice.message()).thenReturn(mockChoiceMessage);

        CompletionResponse realResponse = CompletionResponse.builder()
          .choices(List.of(mockChoice))
          .build();

        when(gigaChatClient.completions(any())).thenReturn(realResponse);

        // 2. Настройка Redis (устраняем NullPointerException)
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // Метод .set() возвращает void, поэтому дополнительная настройка valueOperations не нужна,
        // Mockito просто проигнорирует вызов.

        // 3. Настройка ObjectMapper
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
          .thenReturn(List.of(true, false));

        // 4. Настройка репозиториев
        // Возвращаем пустой список вопросов, чтобы сработал вызов AI
        when(questionRepository.findAllByTagNameAndDifficulty(anyString(), anyString()))
          .thenReturn(List.of());

        // Запуск и проверка
        assertDoesNotThrow(() -> {
            quizService.generateAndSaveQuestions(1L, "java junior");
        });

        // Проверка, что основные методы были вызваны
        verify(gigaChatClient, atLeastOnce()).completions(any());
        verify(redisTemplate, atLeastOnce()).opsForValue();
    }
}