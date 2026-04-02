package org.example.aicareernav1.service.roadmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.*;
import org.example.aicareernav1.dto.roadmap.response.CheckpointResponse;
import org.example.aicareernav1.dto.roadmap.response.ContentResponse;
import org.example.aicareernav1.dto.roadmap.response.ModuleResponse;
import org.example.aicareernav1.dto.roadmap.response.SkeletonResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.example.aicareernav1.mapper.ContentMapper;
import org.example.aicareernav1.mapper.RoadmapMapper;
import org.example.aicareernav1.repository.roadmap.CheckpointRepository;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.repository.roadmap.TopicRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.roadmap.prompt.Prompts;
import org.example.aicareernav1.service.json.JsonUtilsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления жизненным циклом дорожной карты обучения (Roadmap).
 * Обеспечивает генерацию структуры плана, наполнение этапов контентом через ИИ,
 * управление прогрессом пользователя и адаптивное расширение тем.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapService {

  private final RoadmapRepository roadmapRepository;
  private final TopicRepository topicRepository;
  private final CheckpointRepository checkpointRepository;
  private final GigaChatService gigaChatService;
  private final ObjectMapper objectMapper;

  private final JsonUtilsService jsonUtilsService;
  private final ContentMapper contentMapper;
  private final RoadmapMapper roadmapMapper;



  /**
   * Выполняет полный цикл создания персонализированной дорожной карты.
   * <p>
   * Метод инкапсулирует три этапа:
   * 1. Создание базовой сущности {@link Roadmap} с сохранением контекста (требования вакансии и уровень знаний).
   * 2. Обращение к ИИ для генерации структуры (топиков и чекпоинтов), адаптированной под "пробелы" студента.
   * 3. Активация первого этапа обучения для немедленного доступа к контенту.
   * </p>
   *
   * @param request объект, содержащий название вакансии, требования с рынка и результаты входного теста
   * @return созданная и полностью инициализированная дорожная карта с присвоенным ID
   * @throws RuntimeException если ИИ вернул некорректный JSON или произошла ошибка при сохранении в БД
   */
  @Transactional
  public Roadmap generateFullRoadmap(RoadmapGenerationRequest request) {
    log.info("Начало полной генерации для: {}", request.getJobTitle());

    // 1. Создаем саму сущность Roadmap
    // Сохраняем требования и результаты теста в поле userContext
    String context = String.format("Требования: %s. Уровень: %s",
      request.getRequirements(), request.getTestResult());

    Roadmap roadmap = Roadmap.builder()
      .targetJobTitle(request.getJobTitle())
      .userContext(context)
      .build();

    roadmap = roadmapRepository.save(roadmap);

    // 2. Генерируем структуру через ИИ
    generateAndSaveSkeleton(roadmap, request);

    // 3. Активируем первый шаг (чтобы пользователь сразу видел контент первого урока)
    activateFirstCheckpoint(roadmap.getId());

    return roadmap;
  }

  /**
   * Формирует интеллектуальный запрос к нейросети и сохраняет полученную структуру в базу данных.
   * <p>
   * Метод подставляет данные пользователя в {@link Prompts#SKELETON_SYSTEM_PROMPT},
   * десериализует ответ в {@link SkeletonResponse} и выполняет пакетное сохранение
   * связанных сущностей {@link Topic} и {@link Checkpoint}.
   * </p>
   *
   * @param roadmap объект дорожной карты, к которой будут привязаны создаваемые топики
   * @param request контекстные данные для формирования персонализированного промпта
   */
  private void generateAndSaveSkeleton(Roadmap roadmap, RoadmapGenerationRequest request) {
    // Подставляем данные в промпт
    String prompt = Prompts.SKELETON_SYSTEM_PROMPT
      .replace("{jobTitle}", request.getJobTitle())
      .replace("{requirements}", request.getRequirements())
      .replace("{testResult}", request.getTestResult());

    String response = gigaChatService.sendMessage(prompt);
    log.info("Ответ от GigaChat: {}", response);

    SkeletonResponse skeleton = jsonUtilsService.parseObject(response, SkeletonResponse.class);

    if (skeleton == null || skeleton.getTopics() == null || skeleton.getTopics().isEmpty()) {
      log.error("ИИ прислал пустую структуру или невалидный JSON. Topics is null.");
      throw new RuntimeException("Не удалось сгенерировать структуру плана обучения. Попробуйте еще раз.");
    }

    int topicOrder = 1;
    for (TopicDTO tDto : skeleton.getTopics()) {
      Topic topic = Topic.builder()
        .title(tDto.getTopicTitle())
        .orderIndex(topicOrder++)
        .roadmap(roadmap)
        .build();
      topicRepository.save(topic);

      int cpOrder = 1;
      for (CheckpointDTO cpDto : tDto.getCheckpoints()) {
        Checkpoint cp = Checkpoint.builder()
          .title(cpDto.getTitle())
          .description(cpDto.getDescription())
          .status(CheckpointStatus.LOCKED)
          .orderIndex(cpOrder++)
          .topic(topic)
          .build();
        checkpointRepository.save(cp);
      }
    }
  }


  /**
   * Анализирует отзыв пользователя о пройденном этапе и обновляет профиль предпочтений (Learning Style).
   * Результат сжимается до ключевых тезисов для адаптации будущего контента.
   *
   * @param roadmapId    идентификатор дорожной карты
   * @param feedbackText новый текст отзыва от пользователя
   */
  @Transactional
  public void processUserFeedback(Long roadmapId, String feedbackText) {
    Roadmap roadmap = roadmapRepository.findById(roadmapId)
      .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

    String currentNotes = (roadmap.getLearningStyleNotes() != null) ? roadmap.getLearningStyleNotes() : "";

    String userMsg = String.format(Prompts.LEARNING_STYLE_UPDATE_PROMPT, currentNotes, feedbackText);

    try {
      String updatedNotes = gigaChatService.chat(
        "Ты — система долговременной памяти студента. Ты должен дополнять и уточнять знания о нем, не теряя важного.",
        userMsg
      );

      if (updatedNotes != null && !updatedNotes.isBlank()) {
        roadmap.setLearningStyleNotes(updatedNotes.trim());
        roadmapRepository.save(roadmap);
        log.info("Профиль обучения для Roadmap {} обновлен с сохранением контекста.", roadmapId);
      }
    } catch (Exception e) {
      log.error("Ошибка обновления LearningStyle: {}", e.getMessage());
    }
  }


  /**
   * Генерирует обучающий контент (уроки и задачи) для конкретной темы.
   * Использует {@link ContentMapper} для автоматического создания иерархии объектов Module -> Lesson -> Theory/Task.
   *
   * @param checkpointId идентификатор этапа, который нужно наполнить
   */
  @Transactional
  public void fillCheckpointContent(Long checkpointId) {
    log.info("==> [GENERATE CONTENT] Старт генерации для Checkpoint ID: {}", checkpointId);

    Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));

    if (checkpoint.getModule() != null && !checkpoint.getModule().getLessons().isEmpty()) {
      log.info("Контент для чекпоинта '{}' уже существует. Генерация пропущена.", checkpoint.getTitle());
      return;
    }

    Roadmap roadmap = checkpoint.getTopic().getRoadmap();

    // Получаем заметки или ставим дефолт, если профиль еще пуст
    String learningStyle = (roadmap.getLearningStyleNotes() != null && !roadmap.getLearningStyleNotes().isBlank())
      ? roadmap.getLearningStyleNotes()
      : "Стиль обучения еще не определен (используй стандартную подачу).";

    // Формируем структурированный запрос
      String userMsg = String.format(
        "ПРОФИЛЬ СТУДЕНТА (Learning Style):\n%s\n\n" +
          "ЗАДАНИЕ:\n" +
          "Создай учебный модуль для специалиста: %s.\n" +
          "Текущая тема: %s.\n" +
          "Контекст темы: %s.",
        learningStyle,
        roadmap.getTargetJobTitle(),
        checkpoint.getTitle(),
        checkpoint.getDescription()
      );

    try {
      String json = fetchValidJson(userMsg, Prompts.CONTENT_SYSTEM_PROMPT, 3);
      log.info("==> [DEBUG] Сырой ответ от GigaChat: {}", json);

      String sanitizedJson = jsonUtilsService.cleanJsonResponse(json);
      log.info("==> [DEBUG] Очищенный JSON: {}", sanitizedJson);

      ContentResponse response = objectMapper.readValue(sanitizedJson, ContentResponse.class);

      if (response != null && response.getModule() != null) {
        // ВЫЗЫВАЕМ ОТДЕЛЬНЫЙ ТРАНЗАКЦИОННЫЙ МЕТОД ДЛЯ СОХРАНЕНИЯ
        saveGeneratedContent(checkpointId, response.getModule());
      }
    } catch (Exception e) {
      log.error("!!! Ошибка генерации: {}", e.getMessage(), e);
    }
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

  /**
   * Получает детальный образовательный контент (модуль) для конкретного этапа.
   * Выполняет автоматический маппинг сущности Module и вложенных уроков в иерархию DTO.
   *
   * @param checkpointId идентификатор этапа обучения
   * @return {@link ModuleResponse}, содержащий список уроков, теорию и задачи, или null, если контент еще не создан
   */
  public ModuleResponse getModuleByCheckpointId(Long checkpointId) {
    // 1. Ищем чекпоинт
    Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint not found"));

    // 2. Если модуля нет — запускаем генерацию
    if (checkpoint.getModule() == null) {
      // Вызываем твой метод генерации (убедись, что он сохраняет модуль в БД)
      fillCheckpointContent(checkpointId);

      // Перечитываем чекпоинт, чтобы получить уже созданный модуль
      checkpoint = checkpointRepository.findById(checkpointId).get();
    }

    // 3. Маппим в Response
    return roadmapMapper.toModuleResponse(checkpoint.getModule());
  }


  /**
   * Сохраняет сгенерированный ИИ контент (модуль) в базу данных.
   * * Метод устанавливает двусторонние связи между всеми вложенными сущностями,
   * что критично для корректной работы JPA (установки Foreign Keys в БД).
   * * @param checkpointId ID этапа
   * @param moduleDto    Данные от ИИ
   */
  @Transactional
  public void saveGeneratedContent(Long checkpointId, ModuleDTO moduleDto) {
    log.info("==> [DB SAVE] Сохранение модуля для Checkpoint ID: {}", checkpointId);

    // 1. Находим родительский чекпоинт
    Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));

    // 2. Маппим DTO в сущность Module
    Module module = contentMapper.toEntity(moduleDto);

    // 3. Устанавливаем связь Module <-> Checkpoint
    module.setCheckpoint(checkpoint);
    checkpoint.setModule(module);

    // 4. Проходим по дереву сущностей и проставляем обратные ссылки (Back-references)
    if (module.getLessons() != null) {
      for (Lesson lesson : module.getLessons()) {
        // Связь Lesson -> Module
        lesson.setModule(module);

        if (lesson.getTheory() != null) {
          Theory theory = lesson.getTheory();
          // Связь Theory -> Lesson
          theory.setLesson(lesson);

          if (theory.getResources() != null) {
            for (Resource resource : theory.getResources()) {
              // Связь Resource -> Theory
              resource.setTheory(theory);
            }
          }
        }

        // Если у тебя есть задачи (Tasks)
        if (lesson.getTasks() != null) {
          lesson.getTasks().forEach(task -> task.setLesson(lesson));
        }
      }
    }

    // 5. Сохраняем чекпоинт. Благодаря CascadeType.ALL в Checkpoint.java,
    // модуль и все его уроки сохранятся автоматически.
    checkpointRepository.save(checkpoint);

    log.info("<== [DB SUCCESS] Модуль и уроки успешно зафиксированы в БД");
  }

  /**
   * Реализует функционал адаптивного обучения "Углубиться в тему".
   * 1. Находит текущий этап и сдвигает индексы всех последующих этапов в топике.
   * 2. Генерирует новый "уточняющий" чекпоинт через ИИ на основе запроса пользователя.
   * 3. Маппит полученный от ИИ DTO в сущность и сохраняет её в БД.
   * 4. Инициирует немедленную генерацию уроков для нового этапа.
   *
   * @param currentCheckpointId ID этапа, после которого вставляется новый блок
   * @param userRequest          текст запроса пользователя (например, "хочу подробнее про индексы в SQL")
   * @return {@link CheckpointResponse} созданного и наполненного этапа
   * @throws RuntimeException если произошла ошибка при парсинге ответа ИИ или сохранении данных
   */
  @Transactional
  public CheckpointResponse deepenTopic(Long currentCheckpointId, String userRequest) {
    log.info("Запрос на углубление темы после Checkpoint ID: {}", currentCheckpointId);
    Checkpoint current = checkpointRepository.findById(currentCheckpointId)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));

    // Сдвигаем индексы последующих этапов в этом топике
    List<Checkpoint> followers = checkpointRepository.findAllByTopicIdOrderByOrderIndexAsc(current.getTopic().getId());
    followers.stream()
      .filter(cp -> cp.getOrderIndex() > current.getOrderIndex())
      .forEach(cp -> cp.setOrderIndex(cp.getOrderIndex() + 1));

    checkpointRepository.saveAll(followers); //для hibernate
    String json = jsonUtilsService.cleanJsonResponse(fetchValidJson(userRequest, Prompts.DEEPEN_TOPIC_SYSTEM_PROMPT, 2));

    try {
      DeepenCheckpointDTO dto = objectMapper.readValue(json, DeepenCheckpointDTO.class);

      // Используем маппер для создания сущности
      Checkpoint deep = roadmapMapper.toEntity(dto);
      deep.setTopic(current.getTopic());
      deep.setOrderIndex(current.getOrderIndex() + 1);
      deep.setParentCheckpointId(current.getId());

      checkpointRepository.save(deep);

      // Помечаем текущий как выполненный (так как мы пошли вглубь)
      current.setStatus(CheckpointStatus.COMPLETED);
      checkpointRepository.save(current);

      // Сразу генерируем контент для нового этапа
      fillCheckpointContent(deep.getId());

      return roadmapMapper.toCheckpointResponse(deep);
    } catch (Exception e) {
      log.error("Ошибка при создании углубленного этапа: {}", e.getMessage());
      throw new RuntimeException("Не удалось создать углубление темы");
    }
  }


  /**
   * Вспомогательный метод для получения гарантированно валидного JSON от ИИ.
   * Выполняет повторные запросы в случае получения некорректной структуры.
   *
   * @param context      входные данные пользователя
   * @param systemPrompt системная инструкция
   * @param attempts     количество попыток генерации
   * @return строка с чистым JSON
   */
  private String fetchValidJson(String context, String systemPrompt, int attempts) {
    String lastResponse = "";
    for (int i = 0; i < attempts; i++) {
      log.info("Запрос к ИИ (Попытка {}/{})", i + 1, attempts);
      lastResponse = gigaChatService.chat(systemPrompt, context);

      String cleaned = jsonUtilsService.cleanJsonResponse(lastResponse);

      if (jsonUtilsService.isValidJson(cleaned)) {
        return cleaned;
      }

      log.warn("Попытка {}: ИИ выдал невалидный JSON", i + 1);
      context = "Исправь JSON и выведи только чистый код: " + lastResponse;
    }
    return "{}";
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
          .findFirst().ifPresent(cp -> {
            cp.setStatus(CheckpointStatus.ACTIVE);
            checkpointRepository.save(cp);
            fillCheckpointContent(cp.getId());
          });
      });
  }


  /**
   * Рассчитывает процент завершения обучения по всем этапам Roadmap.
   *
   * @param roadmapId ID дорожной карты
   * @return прогресс от 0.0 до 100.0
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
}