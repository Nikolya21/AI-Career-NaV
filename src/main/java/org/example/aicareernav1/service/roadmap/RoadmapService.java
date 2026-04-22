package org.example.aicareernav1.service.roadmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.*;
import org.example.aicareernav1.dto.roadmap.checkpoint.CheckpointDTO;
import org.example.aicareernav1.dto.roadmap.response.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.example.aicareernav1.enums.CheckpointType;
import org.example.aicareernav1.mapper.ContentMapper;
import org.example.aicareernav1.mapper.RoadmapMapper;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.repository.roadmap.CheckpointRepository;
import org.example.aicareernav1.repository.roadmap.LessonRepository;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.repository.roadmap.TopicRepository;
import org.example.aicareernav1.service.roadmap.prompt.GeneralPrompts;
import org.example.aicareernav1.service.util.JsonUtilsService;
import org.example.aicareernav1.service.yandexGpt.YandexGptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * Сервис для управления жизненным циклом динамической дорожной карты обучения (Roadmap).
 * <p>
 * Реализует архитектуру на основе графа, где обучение начинается с корневого узла (ROOT),
 * расходясь в основные темы (MAIN). Сервис поддерживает стратегию отложенной генерации (Lazy Loading):
 * структура уроков и их содержание формируются только в момент обращения пользователя к конкретному этапу.
 * </p>
 * * <p>Основные функции:</p>
 * <ul>
 * <li>Генерация начального скелета графа на основе требований вакансии и тестов студента.</li>
 * <li>Инициализация "пустых" чекпоинтов при первом входе (создание списка уроков-скелетов).</li>
 * <li>Адаптивное расширение карты (Deepen) по специфическим запросам пользователя.</li>
 * <li>Управление конфигурацией обучения (стиль, уровень, тон подачи) через обратную связь.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final TopicRepository topicRepository;
    private final CheckpointRepository checkpointRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final YandexGptService llmService;
    private final RoadmapConfigService configService;
    private final CheckpointService checkpointService;

    private final JsonUtilsService jsonUtilsService;
    private final ContentMapper contentMapper;
    private final RoadmapMapper roadmapMapper;
    private final ObjectMapper objectMapper;


    /**
     * Создает новую персонализированную дорожную карту "с нуля".
     * <p>
     * Процесс включает:
     * 1. Инициализацию {@link RoadmapConfig} с базовыми предпочтениями.
     * 2. Создание неизменяемого ROOT-узла, служащего точкой входа в граф.
     * 3. Генерацию набора MAIN-чекпоинтов (заглушек) на основе анализа дефицита знаний студента.
     * 4. Автоматическую активацию первого логического этапа.
     * </p>
     *
     * @param request данные о целевой вакансии, требованиях рынка и текущем уровне знаний студента.
     * @return {@link RoadmapResponse} содержащий структуру графа для первичной отрисовки.
     */
    @Transactional
    public RoadmapResponse generateFullRoadmap(RoadmapGenerationRequest request) {
        log.info("=== НАЧАЛО ГЕНЕРАЦИИ ROADMAP ДЛЯ: {} ===", request.getJobTitle());

        // 1. Создаем дефолтный конфиг (потом он будет меняться через Feedback)
        RoadmapConfig config = configService.createDefaultConfig();
        config.setMainDomain(request.getJobTitle());

        // 2. Создаем и сохраняем Roadmap
        Roadmap roadmap = roadmapRepository.save(Roadmap.builder()
                .targetJobTitle("Roadmap for " + request.getJobTitle())
                .config(config)
                .build());
        log.info(">>>> [STEP 1] Roadmap сохранен. ID: {}", roadmap.getId());

        // 3. Создаем ROOT Node — "Входная точка"
        Checkpoint root = checkpointService.createRootCheckpoint(roadmap, request.getJobTitle());

        log.info(">>>> [STEP 2] ROOT-узел создан. ID: {}", root.getId());

        // 3. Генерируем и сохраняем начальные MAIN точки, привязанные к ROOT
        generateAndSaveSkeleton(roadmap, root, request);

        activateFirstCheckpoint(roadmap.getId());
        log.info(">>>> [FINISH] Генерация завершена для Roadmap ID: {}", roadmap.getId());

        return roadmapMapper.toResponse(roadmap);
    }


    /**
     * Активирует основной этап обучения (MAIN Checkpoint), наполняя его структурой уроков.
     * <p>
     * Метод вызывается, когда пользователь впервые кликает на "пустой" узел темы на карте.
     * Обращается к ИИ для формирования списка из 3-5 уроков-скелетов, которые в дальнейшем
     * будут наполняться теорией через {@link LessonService}.
     * </p>
     *
     * @param checkpointId идентификатор чекпоинта-заглушки.
     * @return {@link CheckpointResponse} с обновленными данными и списком созданных уроков.
     * @throws EntityNotFoundException если чекпоинт не найден в базе данных.
     */
    @Transactional
    public CheckpointResponse getOrStartCheckpoint(Long checkpointId) {
        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new EntityNotFoundException("Чекпоинт не найден"));
        System.out.println(checkpoint.getRoadmap());
        // Если это основной этап и он еще не "развернут" (нет уроков)
        if (checkpoint.getType() == CheckpointType.MAIN &&
                (checkpoint.getModule() == null || checkpoint.getModule().getLessons().isEmpty())) {

            log.info("==> [AUTO-START] Инициализация чекпоинта ID: {}", checkpointId);
            return checkpointService.generateMainCheckpoint(
                    checkpoint.getParentCheckpoint().getId(),
                    checkpoint.getTitle(),
                    checkpoint.getRoadmap().getConfig(),
                    checkpoint.getRoadmap(),
                    checkpoint
            );
        }

        // Если уже активирован — просто возвращаем текущее состояние
        return roadmapMapper.toCheckpointResponse(checkpoint);
    }


    /**
     * Формирует интеллектуальный запрос к LLM для создания первичного набора тем.
     * <p>
     * Результат парсится в {@link SkeletonResponse}, после чего создаются сущности {@link Topic}
     * и связанные с ними {@link Checkpoint} с типом MAIN, привязанные к корневому узлу.
     * </p>
     *
     * @param roadmap  сущность дорожной карты.
     * @param rootNode созданный ранее корневой узел графа.
     * @param request  входные параметры для формирования контекста промпта.
     * @return десериализованный ответ от ИИ со структурой топиков.
     */
    private SkeletonResponse generateAndSaveSkeleton(Roadmap roadmap, Checkpoint rootNode, RoadmapGenerationRequest request) {
        log.info(">>>> [AI] Запрос скелета у GigaChat...");
        // 1. Формируем промпт (используем твой шаблон)
        String prompt = GeneralPrompts.SKELETON_SYSTEM_PROMPT
                .replace("{jobTitle}", request.getJobTitle())
                .replace("{requirements}", request.getRequirements())
                .replace("{testResult}", request.getTestResult());

        String rawResponse = llmService.sendMessage(prompt);
        log.debug(">>>> [AI] Raw response: {}", rawResponse);

        // Очищаем и парсим JSON
        String cleanedJson = jsonUtilsService.cleanJsonResponse(rawResponse);
        SkeletonResponse skeleton = jsonUtilsService.parseObject(cleanedJson, SkeletonResponse.class);

        if (skeleton == null || skeleton.getTopics() == null || skeleton.getTopics().isEmpty()) {
            log.error("ИИ прислал пустую структуру. План не может быть построен.");
            throw new RuntimeException("Ошибка генерации структуры плана.");
        }

        log.info(">>>> [PROCESS] ИИ предложил {} топиков. Начинаю сохранение связей...", skeleton.getTopics().size());

        // ВАЖНО 1: Инициализируем список топиков у Roadmap (чтобы он не был null)
        if (roadmap.getTopics() == null) {
            roadmap.setTopics(new ArrayList<>());
        }

        // 2. Обработка и сохранение в базу
        int topicOrder = roadmap.getTopics().size();
        List<Checkpoint> checkpoints = new ArrayList<>();
        for (TopicDTO tDto : skeleton.getTopics()) {
            // Сохраняем топик (как логическую группу)
            Topic topic = Topic.builder()
                    .title(tDto.getTopicTitle())
                    .orderIndex(topicOrder++)
                    .roadmap(roadmap)
                    .checkpoints(new ArrayList<>())
                    .build();
            topic = topicRepository.save(topic); // Сохраняем, чтобы получить ID для связи с CP

            // ВАЖНО: Добавляем топик в объект roadmap, чтобы он попал в итоговый JSON
            roadmap.addTopic(topic);
            log.info("  + Топик сохранение: '{}' (ID: {})", topic.getTitle(), topic.getId());

            int cpOrder = 1;
            for (CheckpointDTO cpDto : tDto.getCheckpoints()) {
                // Создаем MAIN чекпоинт, который "растет" из ROOT
                Checkpoint mainCp = Checkpoint.builder()
                        .title(cpDto.getTitle())
                        .description(cpDto.getDescription())
                        .type(CheckpointType.MAIN) // Указываем, что это основной этап
                        .status(CheckpointStatus.PENDING) // Статус: ожидает инициализации
                        .orderIndex(cpOrder++)
                        .topic(topic)
                        .parentCheckpoint(rootNode) // ВАЖНО: связываем с ROOT для графа
                        .roadmap(roadmap)
                        .build();

                checkpoints.add(mainCp);
                log.debug("    - Подготовка чекпоинта: '{}'", mainCp.getTitle());

            }
            checkpointRepository.saveAll(checkpoints);
            topic.getCheckpoints().addAll(checkpoints);
            log.info("    Связано {} чекпоинтов с топиком '{}'", checkpoints.size(), topic.getTitle());
        }

        return skeleton;
    }


    /**
     * Обновляет профиль предпочтений студента (Learning Style) на основе обратной связи.
     * <p>
     * Текст отзыва анализируется ИИ, после чего обновляются параметры тона подачи,
     * сложности и стиля изложения в {@link RoadmapConfig}.
     * </p>
     *
     * @param roadmapId    ID дорожной карты.
     * @param feedbackText текст отзыва (например: "слишком сложно", "хочу больше примеров кода").
     */
    @Transactional
    public void processUserFeedback(Long roadmapId, String feedbackText) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

        configService.updateConfigFromUserText(roadmap, feedbackText);
    }


    /**
     * Получает полную структуру Roadmap (топики и чекпоинты) для отображения.
     * Использует жадную загрузку (size()), чтобы избежать LazyInitializationException в View.
     *
     * @param id идентификатор дорожной карты
     * @return сущность Roadmap со всеми вложенными данными
     */
    @Transactional(readOnly = true)
    public Roadmap getRoadmapWithTopics(Long id) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

        // Инициализируем коллекции для работы в View
        roadmap.getTopics().forEach(topic -> topic.getCheckpoints().size());

        return roadmap;
    }


    /**
     * Возвращает краткую информацию об этапе в формате DTO.
     * Используется контроллерами для отображения метаданных чекпоинта без загрузки тяжелого контента.
     *
     * @param id уникальный идентификатор чекпоинта
     * @return {@link CheckpointResponse} с основными данными этапа
     * @throws EntityNotFoundException если чекпоинт с указанным ID не найден в БД
     */
    @Transactional(readOnly = true)
    public CheckpointResponse getCheckpointResponse(Long id) {
        return checkpointRepository.findById(id)
                .map(roadmapMapper::toCheckpointResponse)
                .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));
    }


//    /**
//     * Возвращает детальный контент (модуль) для этапа обучения.
//     * <p>
//     * <b>ВАЖНО:</b> Метод предназначен для получения уже инициализированного контента.
//     * Если этап не был активирован через {@link #startMainCheckpoint(Long)}, будет выброшено исключение.
//     * </p>
//     *
//     * @param checkpointId ID этапа.
//     * @return {@link ModuleResponse} со списком уроков и их состоянием.
//     * @throws IllegalStateException если модуль для данного чекпоинта еще не был сгенерирован.
//     */
//    public ModuleResponse getModuleByCheckpointId(Long checkpointId) {
//        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
//                .orElseThrow(() -> new EntityNotFoundException("Checkpoint not found"));
//
//        if (checkpoint.getModule() == null) {
//            // бросить исключение, что этап еще не инициализирован
//            throw new IllegalStateException("Этап еще не активирован. Сначала вызовите startMainCheckpoint");
//        }
//
//        return roadmapMapper.toModuleResponse(checkpoint.getModule());
//    }



    /**
     * Реализует функционал динамического расширения графа "Углубиться в тему" (Deepen).
     * <p>
     * Создает новый узел типа DEEPEN, привязанный к текущему чекпоинту, и мгновенно
     * инициирует генерацию теории по специфическому вопросу пользователя.
     * </p>
     *

     * @param userRequest  конкретный вопрос или тема для углубленного изучения.
     * @return ответ с данными нового чекпоинта и готовым контентом урока.
     */
    //todo: нужно все-таки добавить связь с Lesson, откуда выходит - пока ее нет (вроде не критично, но вдальнейшем для анализа понадобиться)
//    @Transactional
//    public CheckpointResponse deepenTopicProcess(Long checkpointId, String userRequest) {
//        // 1. Находим текущий контекст
//        Checkpoint parent = checkpointRepository.findById(checkpointId)
//                .orElseThrow(() -> new EntityNotFoundException("Checkpoint not found"));
//        Roadmap roadmap = parent.getRoadmap();
//        RoadmapConfig config = roadmapRepository.findConfigByRoadmapId(roadmap.getId())
//                .orElse(configService.createDefaultConfig());
//
//        return checkpointService.deepenTopic(parent.getId(), userRequest, config, roadmap);
//    }

    @Transactional
    public CheckpointResponse deepenTopicProcess(Long lessonId, String userRequest) {
        Checkpoint parentCheckpoint = checkpointRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Checkpoint not found"));

        Lesson parentLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Checkpoint not found"));

        Roadmap roadmap = parentCheckpoint.getRoadmap();
        RoadmapConfig config = roadmapRepository.findConfigByRoadmapId(roadmap.getId())
                .orElse(configService.createDefaultConfig());

        return checkpointService.deepenTopic(parentCheckpoint, parentLesson, userRequest, config, roadmap);
    }

    /**
     * Активирует первый этап дорожной карты для начала обучения.
     *
     * @param roadmapId ID дорожной карты
     */
    private void activateFirstCheckpoint(Long roadmapId) {
        topicRepository.findAllByRoadmapIdOrderByOrderIndexAsc(roadmapId).stream()
                .findFirst().ifPresent(t -> {
                    checkpointRepository.findAllByTopicIdOrderByOrderIndexAsc(t.getId()).stream()
                            .skip(1).findFirst().ifPresent(cp -> {
                                cp.setStatus(CheckpointStatus.ACTIVE);
                                checkpointRepository.save(cp);
                            });
                });
    }


    /**
     * Рассчитывает суммарный прогресс прохождения дорожной карты.
     * <p>
     * Прогресс вычисляется как отношение количества чекпоинтов в статусе COMPLETED
     * к общему количеству узлов в графе (исключая ROOT).
     * </p>
     *
     * @param roadmapId ID дорожной карты.
     * @return значение прогресса от 0.0 до 100.0.
     */
    @Transactional(readOnly = true)
    public double calculateProgress(Long roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

        List<Checkpoint> allCheckpoints = roadmap.getTopics().stream()
                .flatMap(t -> t.getCheckpoints().stream())
                .toList();

        if (allCheckpoints.isEmpty()) return 0.0;

        long completedCount = allCheckpoints.stream()
                .filter(cp -> cp.getStatus() == CheckpointStatus.COMPLETED)
                .count();

        return (double) completedCount / allCheckpoints.size() * 100;
    }


    public Roadmap getRoadmapById(Long id) {
        return roadmapRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Дорожная карта не найдена"));
    }

//    public UserEntity getUserByRoadmapId(Long roadmapId) {
//        //roadmapRepository.get
//
//    }

    @Transactional(readOnly = true)
    public RoadmapResponse getFullGraphResponse(Long roadmapId) {
        log.info(">>>> [GET] Запрос графа для ID: {}", roadmapId);

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap not found"));

        // Логируем проверку связей перед маппингом
        log.info(">>>> [CHECK] Проверка связей в БД для Roadmap '{}':", roadmap.getTargetJobTitle());
        roadmap.getTopics().forEach(t -> {
            log.info("  Топик: '{}' (ID: {}), Чекпоинтов: {}", t.getTitle(), t.getId(), t.getCheckpoints().size());
            t.getCheckpoints().forEach(cp -> {
                if (cp.getParentCheckpoint() == null) {
                    log.error("    [!] Чекпоинт '{}' (ID: {}) НЕ ИМЕЕТ родителя (parentCheckpoint IS NULL)", cp.getTitle(), cp.getId());
                } else {
                    log.debug("    Чекпоинт '{}' -> Родитель ID: {}", cp.getTitle(), cp.getParentCheckpoint().getId());
                }
            });
        });

        return roadmapMapper.toResponse(roadmap);
    }
}