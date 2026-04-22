package org.example.aicareernav1.service.roadmap.theory.strategy;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.external.pythonRAG.ChunkResponse;
import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SaveRequest;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;

import org.example.aicareernav1.mapper.RagIntegrationMapper;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.integration.PythonIntegrationService;
import org.example.aicareernav1.service.roadmap.RoadmapConfigService;
import org.example.aicareernav1.service.roadmap.prompt.TheoryPrompts;
import org.example.aicareernav1.service.util.LlmResponseParserService;
import org.example.aicareernav1.service.yandexGpt.YandexGptService;
import org.springframework.stereotype.Component;

import java.util.List;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenerateTheoryStrategy implements TheoryProcessingStrategy {

    private final PythonIntegrationService pythonClient;
    // Предположим, тут ваш сервис для работы с LLM (GigaChat/GPT/Claude)
    private final YandexGptService llmService;
    private final LlmResponseParserService llmParser;
    private final RagIntegrationMapper ragMapper;
    private final RoadmapConfigService configService;
    private final RoadmapRepository roadmapRepository;

    @Override
    public boolean supports(String status) {
        return "NEED_GENERATION".equals(status) || "NOT_FOUND".equals(status);
    }

    @Override
    public Theory process(GatewayResponse response, SearchRequest request, Lesson lesson, RoadmapConfig config, String context) {
        // Достаем профиль из роадмапа (связь @OneToOne)
//        RoadmapConfig config = roadmapRepository.findConfigByLessonId(lesson.getId())
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "RoadmapConfig not found for lesson with ID: " + lesson.getId()
//                ));
        //todo: когда все проеверю, заменить:
        //  RoadmapConfig config = roadmapRepository.findConfigByLessonId(lesson.getId())
        //      .orElseGet(() -> configService.createDefaultConfig());

        // 1. Формируем единую строку контекста (теги + стиль + тон)
        String fullLearningContext = configService.getFullContextString(config);

        // 3. Генерируем теорию через LLM на основе чанков из RAG, используя эти же настройки стиля
        String rawLlmResponse = generateLessonFromChunks(response.getChunks(), request.getQuery(), fullLearningContext, config.getMainDomain(), context);

        LlmResponseParserService.ParsedLlmContent cleanGeneratedMarkdown = llmParser.parseTheoryResponse(rawLlmResponse);

        lesson.setSummary(cleanGeneratedMarkdown.getSummary());

        Theory theory = Theory.builder()
                .text(cleanGeneratedMarkdown.getContent())
                .tags(cleanGeneratedMarkdown.getTags())
                .lesson(lesson)
                .build();


        // 4. Создание SaveRequest для Python RAG
        SaveRequest saveRequest = ragMapper.toSaveRequest(theory, lesson, cleanGeneratedMarkdown.getTags(), request.getQuery());

        // 5. Асинхронное сохранение в Python
        pythonClient.saveProcessedContent(saveRequest);

        lesson.setTheory(theory);
        return theory;
    }

    private String generateLessonFromChunks(List<ChunkResponse> chunks, String userQuery, String contextLearning, String mainDomain, String context) {
        String chunksText = chunks.stream()
                .map(ChunkResponse::getContent)
                .collect(Collectors.joining("\n---\n"));

        return llmService.sendMessage(TheoryPrompts.getGenerateTheoryPrompt(userQuery, chunksText, contextLearning, mainDomain, context));
    }
}