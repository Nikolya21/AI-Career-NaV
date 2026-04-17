package org.example.aicareernav1.service.roadmap.theory.strategy;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.external.pythonRAG.ChunkResponse;
import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SaveRequest;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.example.aicareernav1.repository.roadmap.LessonRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.integration.PythonIntegrationService;
import org.example.aicareernav1.service.roadmap.theory.prompt.Prompts;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GenerateTheoryStrategy implements TheoryProcessingStrategy {

    private final PythonIntegrationService pythonClient;
    private  final LessonRepository lessonRepository;
    // Предположим, тут ваш сервис для работы с LLM (GigaChat/GPT/Claude)
    private final GigaChatService llmService;

    @Override
    public boolean supports(String status) {
        return "NEED_GENERATION".equals(status);
    }

    @Override
    public Theory process(GatewayResponse response, SearchRequest request, Lesson lesson) {
        String learningStyle = lessonRepository.findRoadmapNotesByLessonId(lesson.getId());
        if (learningStyle == null) {
            learningStyle = "Standard technical documentation style";
        }
        // 1. Адаптируем запрос для поиска (Query Expansion)
        String refinedQuery = queryAdaptation(request.getQuery(), learningStyle);

        // 2. Генерируем теорию через LLM на основе чанков из RAG, используя эти же настройки стиля
        String generatedMarkdown = generateLessonFromChunks(response.getChunks(), refinedQuery, learningStyle);

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

    private String generateLessonFromChunks(List<ChunkResponse> chunks, String userQuery, String userTags) {
        StringBuilder chunksText = new StringBuilder();
        for (ChunkResponse chunk : chunks) {
            chunksText.append(chunk.getContent()).append("\n---\n");
        }
        return Prompts.getGenerateTheoryPrompt(userQuery, chunksText.toString(), userTags);
    }

    private String queryAdaptation(String userQuery, String userTags) {
        return llmService.sendMessage(Prompts.getQueryAdaptationForSearchPrompt(userQuery, userTags));
    }
}