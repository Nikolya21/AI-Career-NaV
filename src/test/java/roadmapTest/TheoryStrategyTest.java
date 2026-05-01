package roadmapTest;

import org.example.aicareernav1.dto.external.pythonRAG.ChunkResponse;
import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SaveRequest;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.example.aicareernav1.mapper.RagIntegrationMapper;
import org.example.aicareernav1.service.integration.PythonIntegrationService;
import org.example.aicareernav1.service.roadmap.RoadmapConfigService;
import org.example.aicareernav1.service.roadmap.theory.strategy.GenerateTheoryStrategy;
import org.example.aicareernav1.service.yandexGpt.YandexGptService; // Проверь правильность пути
import org.example.aicareernav1.service.util.LlmResponseParserService; // Сервис парсинга JSON
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TheoryStrategyTest {

    @Mock private YandexGptService llmService;
    @Mock private PythonIntegrationService pythonClient;
    @Mock private LlmResponseParserService llmParser;
    @Mock private RagIntegrationMapper ragMapper;

    // ДОБАВЬТЕ ЭТОТ МОК:
    @Mock private RoadmapConfigService configService;

    @InjectMocks
    private GenerateTheoryStrategy generateStrategy;

    @Test
    void process_ShouldIntegrateWithPythonRagAfterGeneration() {
        // 1. Подготовка SearchRequest (для userQuery)
        SearchRequest request = new SearchRequest();
        request.setQuery("Spring Security");

        // 2. Подготовка Lesson (для формирования связей)
        Lesson lesson = new Lesson();
        lesson.setTitle("Introduction to Auth");

        // 3. Подготовка RoadmapConfig !!! КРИТИЧЕСКИЙ МОМЕНТ !!!
        RoadmapConfig config = new RoadmapConfig();
        config.setMainDomain("Java Development"); // Это поле используется в generateLessonFromChunks
        // Если в промпте используется {userTags} из объекта config, установите и его:
        // config.setUserTags("Beginner, Analogies");

        // 4. Подготовка RAG Chunks (не должны быть null)
        ChunkResponse chunk = new ChunkResponse();
        chunk.setContent("Some technical documentation content");
        chunk.setResources(List.of("http://example.com"));

        GatewayResponse response = new GatewayResponse();
        response.setChunks(List.of(chunk));

        // 5. Настройка моков для сервисов
        // configService должен возвращать не null
        when(configService.getFullContextString(any())).thenReturn("Learning Context: Junior level");

        // Формируем ParsedLlmContent через Builder
        LlmResponseParserService.ParsedLlmContent parsed = LlmResponseParserService.ParsedLlmContent.builder()
          .tags(List.of("security", "web"))
          .content("## Generated Theory Content")
          .summary("Summary of the lesson")
          .build();

        when(llmService.sendMessage(anyString())).thenReturn("METADATA: Java, Security === Content");
        when(llmParser.parseTheoryResponse(anyString())).thenReturn(parsed);

        SaveRequest mockSaveRequest = mock(SaveRequest.class);
        when(ragMapper.toSaveRequest(any(), any(), any(), any())).thenReturn(mockSaveRequest);

        // 6. Выполнение (context "User History" не должен быть null)
        Theory result = generateStrategy.process(response, request, lesson, config, "User History");

        // 7. Проверки
        assertNotNull(result);
        assertEquals("## Generated Theory Content", result.getText());
        verify(pythonClient).saveProcessedContent(any(SaveRequest.class));
    }
}