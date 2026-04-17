package org.example.aicareernav1.service.roadmap.theory.strategy;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SaveRequest;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.integration.PythonIntegrationService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GenerateTheoryStrategy implements TheoryProcessingStrategy {

    private final PythonIntegrationService pythonClient;
    // Предположим, тут ваш сервис для работы с LLM (GigaChat/GPT/Claude)
    private final GigaChatService llmService;

    @Override
    public boolean supports(String status) {
        return "NEED_GENERATION".equals(status);
    }

    @Override
    public Theory process(GatewayResponse response, SearchRequest request, Lesson lesson) {
        // 1. Генерируем контент через LLM на основе чанков из RAG
        String generatedMarkdown = llmService.generateLessonFromChunks(response.getChunks(), request.getQuery());

        // 2. Создаем сущность Theory
        // 2. Создание SaveRequest для Python RAG
        SaveRequest saveRequest = SaveRequest.builder()
                .query(request.getQuery())
                .content(generatedMarkdown)
                .contentType("finished_lesson")
                .tags(request.getTags())
                .resources(new ArrayList<>()) // Можно извлечь ссылки из сгенерированного текста
                .metadata(Map.of("lesson_id", lesson.getId()))
                .build();

        // 3. Асинхронное сохранение в Python
        pythonClient.saveProcessedContent(saveRequest);

        // 4. Создание и привязка сущности Theory
        Theory theory = Theory.builder()
                .text(generatedMarkdown)
                .lesson(lesson)
                .build();

        lesson.setTheory(theory);
        return theory;
    }
}