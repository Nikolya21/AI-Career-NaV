package org.example.aicareernav1.service.roadmap;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.response.LessonResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Checkpoint;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.enums.CheckpointType;
import org.example.aicareernav1.mapper.ContentMapper;
import org.example.aicareernav1.repository.roadmap.CheckpointRepository;
import org.example.aicareernav1.repository.roadmap.LessonRepository;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.service.roadmap.prompt.CheckpointPrompts;
import org.example.aicareernav1.service.roadmap.theory.TheoryOrchestrator;
import org.example.aicareernav1.service.yandexGpt.YandexGptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final RoadmapRepository roadmapRepository;
    private final CheckpointRepository checkpointRepository;

    private final TheoryOrchestrator theoryOrchestrator;
    private final RoadmapConfigService configService;
    private final YandexGptService llmService;
    private final ContentMapper contentMapper;


    /**
     * Получает заголовок урока по ID и userQuery.
     * Используется, когда пользователь кликает на "Углубиться" урок (сценарий с DEEPEN Checkpoint).
     */
    @Transactional
    public LessonResponse fillLessonContent(Long lessonId, String userQuery) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        RoadmapConfig config = roadmapRepository.findConfigByLessonId(lessonId).orElse(configService.createDefaultConfig());

        String adaptationQuery = queryAdaptation(userQuery, config);

        // Собираем контекс
        String context = collectContext(lesson);
        if (context.isEmpty()) {
            context = "Это только начало обучения... Контекста пока нет...";
        }
        // Делегируем всю сложную логику (RAG, стратегии, LLM) оркестратору
        // Используем название урока в качестве поискового запроса
        theoryOrchestrator.getTheoryForLesson(lessonId, adaptationQuery, config, context);

        // Возвращаем обновленный урок через маппер
        return contentMapper.toResponse(lesson);
    }

    /**
     * Получает заголовок урока по ID и запускает процесс наполнения контентом.
     * Используется, когда пользователь кликает на "пустой" урок (Сценарий с MAIN Checkpoint).
     */
    @Transactional
    public LessonResponse getAndFillLesson(Long lessonId) {
        // 1. Получаем сущность урока
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Урок с ID " + lessonId + " не найден"));

        Checkpoint checkpoint = checkpointRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Checkpoint not found"));

        Checkpoint parentCheckpoint = checkpoint.getParentCheckpoint();

        // 2. Используем заголовок урока как userQuery для твоей логики
        String lessonTitle = lesson.getTitle();
        log.info("==> Запуск генерации контента для урока: '{}'", lessonTitle);

        // 3. Вызываем твой существующий метод
        return fillLessonContent(lessonId, lessonTitle);
    }

    public String collectContext(Lesson lesson) {
        Checkpoint checkpoint = checkpointRepository.findByLessonId(lesson.getId())
                .orElseThrow(() -> new EntityNotFoundException("Checkpoint not found"));

        StringBuilder summarizes = new StringBuilder();
        summarizes.append("Пользователь уже знает: ").append("\n");
        for (Lesson lessonInCheckpoint : checkpoint.getModule().getLessons()) {
            if (lessonInCheckpoint.getTheory() != null && !lessonInCheckpoint.getTheory().getText().isEmpty()) {
                summarizes.append("Тема: ").append(lessonInCheckpoint.getTitle()).append("\n");
                summarizes.append(lessonInCheckpoint.getSummary()).append("\n");
            }
        }

        Checkpoint parentCheckpoint = checkpoint.getParentCheckpoint();
        if (parentCheckpoint.getType() != CheckpointType.ROOT) {
            for (Lesson lessonInCheckpoint : parentCheckpoint.getModule().getLessons()) {
                if (lessonInCheckpoint.getTheory() != null && !lessonInCheckpoint.getTheory().getText().isEmpty()) {
                    summarizes.append("Тема: ").append(lessonInCheckpoint.getTitle()).append("\n");
                    summarizes.append(lessonInCheckpoint.getSummary()).append("\n");
                }
            }
        }
        return summarizes.toString();
    }


    private String queryAdaptation(String userQuery, RoadmapConfig config) {
        return llmService.sendMessage(CheckpointPrompts.getQueryAdaptationForSearchPrompt(userQuery, config));
    }
}
