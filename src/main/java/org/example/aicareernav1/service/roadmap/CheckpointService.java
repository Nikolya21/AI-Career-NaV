package org.example.aicareernav1.service.roadmap;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.checkpoint.CheckpointSkeletonDTO;
import org.example.aicareernav1.dto.roadmap.response.CheckpointResponse;
import org.example.aicareernav1.dto.roadmap.response.LessonResponse;
import org.example.aicareernav1.dto.roadmap.response.TheoryResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.example.aicareernav1.enums.CheckpointType;
import org.example.aicareernav1.mapper.ContentMapper;
import org.example.aicareernav1.mapper.RoadmapMapper;
import org.example.aicareernav1.repository.roadmap.CheckpointRepository;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.roadmap.prompt.CheckpointPrompts;
import org.example.aicareernav1.service.util.JsonUtilsService;
import org.example.aicareernav1.service.util.LlmResponseParserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.aicareernav1.service.roadmap.prompt.CheckpointPrompts.DEEPEN_TOPIC_SYSTEM_PROMPT;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckpointService {

    private final CheckpointRepository checkpointRepository;
    private final RoadmapRepository roadmapRepository;
    private final LlmResponseParserService llmResponseParserService;
    private final GigaChatService llmService; // Для генерации скелета
    private final ContentMapper contentMapper;
    private final RoadmapMapper roadmapMapper;
    private final ObjectMapper objectMapper;
    private final JsonUtilsService jsonUtilsService;
    private final LessonService lessonService;

    private static final int MAX_CHECKPOINT_WORDS = 4;
    private static final int MAX_LESSON_WORDS = 3;

    /**
     * Создает новое ответвление (углубление) от существующего чекпоинта.
     */
    @Transactional
    public CheckpointResponse deepenTopic(Long parentId, String userQuery, RoadmapConfig config) {
        // 1. Находим родителя
        Checkpoint parent = checkpointRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Родительский чекпоинт не найден"));

        // 2. Адаптируем запрос (если нужно для поиска)
        String adaptationQuery = queryAdaptation(userQuery, config);

        // 3. Создаем "скелетную" структуру СНИЗУ ВВЕРХ (или через Cascade)
        // Сначала чекпоинт
        Checkpoint newCheckpoint = Checkpoint.builder()
                .title(userQuery) // Временный заголовок, пока нет ответа от LLM
                .type(CheckpointType.DEEPEN)
                .status(CheckpointStatus.ACTIVE)
                .parentCheckpoint(parent)
                .topic(parent.getTopic())
                .build();

        // Создаем модуль
        Module module = Module.builder()
                .title(userQuery)
                .checkpoint(newCheckpoint)
                .build();
        newCheckpoint.setModule(module);

        // Создаем урок через маппер (у него будет связь с модулем)
        //
        Lesson targetLesson = contentMapper.toSkeletonLesson(userQuery, module);
        module.setLessons(List.of(targetLesson));

        // 4. СОХРАНЯЕМ всё дерево в БД.
        // Благодаря CascadeType.ALL в сущностях, сохранение чекпоинта сохранит и модуль, и урок.
        // Теперь у targetLesson появится ID!
        Checkpoint savedCheckpoint = checkpointRepository.save(newCheckpoint);

        // Получаем сохраненный урок из сохраненного чекпоинта
        Lesson savedLesson = savedCheckpoint.getModule().getLessons().get(0);

        // 5. Теперь, когда в БД есть запись урока с ID, вызываем наполнение
        //
        LessonResponse lessonResponse = lessonService.fillLessonContent(savedLesson.getId(), adaptationQuery);

        // 6. Обновляем заголовок Чекпоинта на основе того, что сгенерировал ИИ для урока
        //
        String beautifulTitle = llmResponseParserService.getTitleFromTheoryResponse(lessonResponse.getTheory().getText());
        savedCheckpoint.setTitle(beautifulTitle);
        savedCheckpoint.getModule().setTitle(beautifulTitle);
        savedLesson.setTitle(beautifulTitle);

        // Возвращаем финальный ответ
        return roadmapMapper.toCheckpointResponse(savedCheckpoint);
    }

    /**
     * Внутренняя логика генерации и нормализации названий
     */
    private CheckpointSkeletonDTO generateSkeletonFromAI(String request, RoadmapConfig config, Checkpoint parent) {
        String studentContext = String.format("Уровень: %s, Стиль: %s",
                config.getTargetLevel(), config.getLearningStyle());

        String prompt = String.format(DEEPEN_TOPIC_SYSTEM_PROMPT, studentContext, parent.getTitle(), request);

        // Вызываем GigaChat
        String rawResponse = llmService.sendMessage(prompt);

        String cleanedJsonAiResponse = jsonUtilsService.cleanJsonResponse(rawResponse);

        // Используем ТВОЙ JsonUtilsService для очистки и парсинга
        // Твой метод cleanAndParse (если он реализован как в сниппете) сделает всю грязную работу
        CheckpointSkeletonDTO dto = jsonUtilsService.parseObject(cleanedJsonAiResponse, CheckpointSkeletonDTO.class);

        if (dto == null) {
            log.error("JsonUtilsService не смог распарсить ответ. Используем fallback.");
            return new CheckpointSkeletonDTO("Advanced: " + parent.getTitle(), "", List.of("Concept Deep Dive"));
        }

        // Применяем нормализацию названий (наш "второй фильтр")
        dto.setTitle(sanitizeTitle(dto.getTitle(), MAX_CHECKPOINT_WORDS));
        dto.setLessonTitles(
                dto.getLessonTitles().stream()
                        .map(t -> sanitizeTitle(t, MAX_LESSON_WORDS))
                        .limit(5)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private String sanitizeTitle(String title, int maxWords) {
        if (title == null || title.isBlank()) return "New Step";
        String clean = title.replaceAll("[\"«».,!?]", "").trim();
        String[] words = clean.split("\\s+");
        if (words.length <= maxWords) return clean;
        return String.join(" ", Arrays.copyOfRange(words, 0, maxWords));
    }

    private String queryAdaptation(String userQuery, RoadmapConfig config) {
        return llmService.sendMessage(CheckpointPrompts.getQueryAdaptationForSearchPrompt(userQuery, config));
    }
}
