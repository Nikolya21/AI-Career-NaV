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
import org.example.aicareernav1.repository.roadmap.TopicRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.roadmap.prompt.CheckpointPrompts;
import org.example.aicareernav1.service.util.JsonUtilsService;
import org.example.aicareernav1.service.util.LlmResponseParserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CheckpointService {

    private final CheckpointRepository checkpointRepository;
    private final RoadmapRepository roadmapRepository;
    private final TopicRepository topicRepository;
    private final LlmResponseParserService llmResponseParserService;
    private final GigaChatService llmService; // Для генерации скелета
    private final RoadmapConfigService roadmapConfigService;
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
    public CheckpointResponse deepenTopic(Long parentId, String userQuery, RoadmapConfig config, Roadmap roadmap) {
        // 1. Находим родителя
        Checkpoint parent = checkpointRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Родительский чекпоинт не найден"));

//        // 2. Адаптируем запрос (если нужно для поиска)
//        String adaptationQuery = queryAdaptation(userQuery, config);

        // 3. Создаем "скелетную" структуру СНИЗУ ВВЕРХ (или через Cascade)
        // Сначала чекпоинт
        Checkpoint newCheckpoint = Checkpoint.builder()
                .title(userQuery) // Временный заголовок, пока нет ответа от LLM
                .type(CheckpointType.DEEPEN)
                .status(CheckpointStatus.ACTIVE)
                .topic(parent.getTopic())
                .roadmap(roadmap)
                .parentCheckpoint(parent)
                .build();

        parent.addChild(newCheckpoint);

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
        LessonResponse lessonResponse = lessonService.fillLessonContent(savedLesson.getId(), userQuery);

        // 6. Обновляем заголовок Чекпоинта на основе того, что сгенерировал ИИ для урока
        if (lessonResponse.getTheory() != null) {
            String beautifulTitle = llmResponseParserService.getTitleFromTheoryResponse(lessonResponse.getTheory().getText());
            if (beautifulTitle != null && !beautifulTitle.isBlank()) {
                savedCheckpoint.setTitle(beautifulTitle);
                savedCheckpoint.getModule().setTitle(beautifulTitle);
                savedLesson.setTitle(beautifulTitle);
                // Мы обновляем объект в памяти, Hibernate сам сделает dirty checking и сохранит в конце транзакции
            }
        }
        // Возвращаем финальный ответ
        return roadmapMapper.toCheckpointResponse(savedCheckpoint);
    }


    /**
     * Создает новый основной чекпоинт (MAIN) со списком пустых уроков (скелетов).
     * Контент уроков генерируется позже по запросу пользователя.
     */
    @Transactional
    public CheckpointResponse generateMainCheckpoint(Long parentId, String topic, RoadmapConfig config, Roadmap roadmap, Checkpoint checkpoint) {
        // 1. Находим родителя (это может быть ROOT или другой MAIN)
        Checkpoint parent = checkpointRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Родительский узел не найден"));

        log.info("Creating new checkpoint. Parent ID: {}, Parent Topic: {}", parentId, parent.getTopic());

        // 2. Идем в GigaChat ТОЛЬКО за структурой (названия чекпоинта и 3-5 уроков)
        CheckpointSkeletonDTO skeletonDTO = generateMainSkeletonFromAI(topic, config, parent);

//        // 3. Создаем структуру чекпоинта
//        Checkpoint newCheckpoint = Checkpoint.builder()
//                .title(skeletonDTO.getTitle())
//                .description("Темы: " + String.join(", ", skeletonDTO.getLessonTitles()))
//                .type(CheckpointType.MAIN)
//                .status(CheckpointStatus.ACTIVE)
//                .roadmap(roadmap)
//                .topic(parent.getTopic())
//                .build();
//
//        // Не забываем синхронизировать связи в памяти!
//        parent.addChild(newCheckpoint);

        // Создаем модуль
        Module module = Module.builder()
                .title(skeletonDTO.getTitle())
                .checkpoint(checkpoint)
                .build();
        checkpoint.setModule(module);

        log.info("Check: Roadmap in newCheckpoint is {}", checkpoint.getRoadmap());

        // 4. Магия ленивой загрузки: создаем список пустых уроков
        List<Lesson> skeletonLessons = skeletonDTO.getLessonTitles().stream()
                .map(lessonTitle -> contentMapper.toSkeletonLesson(lessonTitle, module))
                .collect(Collectors.toList());

        module.setLessons(skeletonLessons);

        // 5. СОХРАНЯЕМ всё дерево.
        // В БД появятся записи уроков с ID, но поле `theory` у них будет null.
        Checkpoint savedCheckpoint = checkpointRepository.save(checkpoint);

        // Возвращаем ответ на фронтенд (фронтенд должен отрисовать эти 3-5 уроков как неактивные/непройденные)
        return roadmapMapper.toCheckpointResponse(savedCheckpoint);
    }

    @Transactional
    public Checkpoint createRootCheckpoint(Roadmap roadmap, String jobTitle) {
        log.info("Инициализация ROOT чекпоинта с базовым топиком для Roadmap ID: {}", roadmap.getId());

        // 1. Создаем или находим "Технический" топик для корня
        // Это нужно, чтобы уйти от null в колонке topic_id
        Topic rootTopic = Topic.builder()
                .title("Основы направления") // Базовое название
                .roadmap(roadmap)
                .build();
        roadmap.addTopic(rootTopic);
        // Сохраняем топик (если он еще не сохранен каскадом)
        topicRepository.save(rootTopic);

        // 2. Создаем ROOT чекпоинт и привязываем его к этому топику
        Checkpoint root = Checkpoint.builder()
                .title(jobTitle)
                .description("Стартовая точка: " + jobTitle)
                .orderIndex(0) // <--- Явно делаем его первым
                .type(CheckpointType.ROOT)
                .status(CheckpointStatus.COMPLETED)
                .roadmap(roadmap)
                .topic(rootTopic) // Теперь здесь НЕ null
                .build();
        rootTopic.addCheckpoint(root);

        log.info("ROOT Checkpoint подготовлен: title={}, topic_id={}", root.getTitle(), (root.getTopic() != null ? root.getTopic().getId() : "NULL"));

        return checkpointRepository.save(root);
    }

    /**
     * Внутренняя логика генерации и нормализации названий
     */
    private CheckpointSkeletonDTO generateMainSkeletonFromAI(String topic, RoadmapConfig config, Checkpoint parent) {
        String studentContext = roadmapConfigService.getFullContextString(config);

        // Передаем заголовок родителя для контекста (например, "Spring Boot")
        //todo: Checkpoint (ROOT): title = "Java Developer", parent = null. --> базовый присет для кореневой вершины
        //      Checkpoint (MAIN): title = "Collections Framework", parent = ROOT.
        //      Lesson (Skeleton): title = "List & ArrayList", module -> MAIN.
        String parentTitle = (parent != null) ? parent.getTitle() : "Начало обучения";

        // Используем НОВЫЙ промпт, который жестко требует 3-5 уроков
        String prompt = String.format(CheckpointPrompts.MAIN_TOPIC_SYSTEM_PROMPT, studentContext, parentTitle, topic);

        String rawResponse = llmService.sendMessage(prompt);
        String cleanedJsonAiResponse = jsonUtilsService.cleanJsonResponse(rawResponse);

        CheckpointSkeletonDTO dto = jsonUtilsService.parseObject(cleanedJsonAiResponse, CheckpointSkeletonDTO.class);

        if (dto == null || dto.getLessonTitles() == null || dto.getLessonTitles().isEmpty()) {
            log.error("JsonUtilsService не смог распарсить MAIN скелет. Используем fallback.");
            return new CheckpointSkeletonDTO(
                    sanitizeTitle(topic, MAX_CHECKPOINT_WORDS),
                    "",
                    List.of("Введение в " + topic, "Основные концепции", "Практика") // Fallback на 3 урока
            );
        }

        // Нормализация названий
        dto.setTitle(sanitizeTitle(dto.getTitle(), MAX_CHECKPOINT_WORDS));
        dto.setLessonTitles(dto.getLessonTitles().stream()
                        .map(t -> sanitizeTitle(t, MAX_LESSON_WORDS))
                        .limit(5) // Гарантируем не больше 5 уроков
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

//    private String queryAdaptation(String userQuery, RoadmapConfig config) {
//        return llmService.sendMessage(CheckpointPrompts.getQueryAdaptationForSearchPrompt(userQuery, config));
//    }
}
