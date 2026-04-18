package org.example.aicareernav1.service.roadmap.theory;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.example.aicareernav1.repository.roadmap.LessonRepository;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.repository.roadmap.TheoryRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.integration.PythonIntegrationService;
import org.example.aicareernav1.service.roadmap.prompt.TheoryPrompts;
import org.example.aicareernav1.service.roadmap.theory.strategy.TheoryProcessingStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheoryOrchestrator {

    private final List<TheoryProcessingStrategy> strategies;
    private final PythonIntegrationService pythonClient;
    private final GigaChatService llmService;
    private final LessonRepository lessonRepository;
    private final TheoryRepository theoryRepository;
    private final RoadmapRepository roadmapRepository;

    @Transactional // Обязательно для работы с БД
    public Theory getTheoryForLesson(Long lessonId, String userQuery) {
        // 1. Загружаем урок
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        // 2. Если теория уже есть в БД — просто отдаем её
        if (lesson.getTheory() != null) {
            return lesson.getTheory();
        }

        // 3. Если нет — идем в Python
        SearchRequest request = SearchRequest.builder()
                .query(userQuery)
                .build();
        GatewayResponse response = pythonClient.searchInRag(request);

        // 4. Выбираем и применяем стратегию
        Theory theory = strategies.stream()
                .filter(s -> s.supports(response.getStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No strategy for: " + response.getStatus()))
                .process(response, request, lesson);

        // 5. Сохраняем. Благодаря CascadeType.ALL в Lesson,
        // достаточно сохранить теорию или обновить урок.
        return theoryRepository.save(theory);
    }

}