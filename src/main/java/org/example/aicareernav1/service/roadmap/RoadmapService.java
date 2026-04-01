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

  private final JsonUtilsService jsonUtils;
  private final ContentMapper contentMapper;
  private final RoadmapMapper roadmapMapper;

  /**
   * Создает базовую структуру (скелет) Roadmap на основе контекста пользователя.
   * Генерирует список блоков (Topic) и тем (Checkpoint), сохраняя их в БД.
   * Использует {@link RoadmapMapper} для преобразования DTO в сущности.
   *
   * @param roadmapId   идентификатор созданной заготовки Roadmap
   * @param userContext описание целей пользователя или текст вакансии
   */
  @Transactional
  public void createSkeleton(Long roadmapId, String userContext) {
    Roadmap roadmap = roadmapRepository.findById(roadmapId)
      .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

    String systemPrompt = String.format(Prompts.SKELETON_SYSTEM_PROMPT, roadmap.getTargetJobTitle());
    String jsonResponse = jsonUtils.cleanJsonResponse(fetchValidJson(userContext, systemPrompt, 2));

    try {
      SkeletonResponse response = objectMapper.readValue(jsonResponse, SkeletonResponse.class);
      int topicOrder = 0;
      for (TopicDTO tDto : response.getTopics()) {
        Topic topic = roadmapMapper.toEntity(tDto);
        topic.setOrderIndex(topicOrder++);
        topic.setRoadmap(roadmap);
        topicRepository.save(topic);

        int cpOrder = 0;
        if (tDto.getCheckpoints() != null) {
          for (CheckpointDTO cpDto : tDto.getCheckpoints()) {
            Checkpoint checkpoint = roadmapMapper.toEntity(cpDto);
            checkpoint.setOrderIndex(cpOrder++);
            checkpoint.setTopic(topic);
            checkpoint.setStatus(CheckpointStatus.LOCKED);
            checkpointRepository.save(checkpoint);
          }
        }
      }
      activateFirstCheckpoint(roadmapId);
    } catch (Exception e) {
      log.error("Ошибка при создании структуры Roadmap: {}", e.getMessage());
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
  public void fillCheckpointContent(Long checkpointId) { // todo: никак учитывает userPreference - исправить
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
      // Запрашиваем контент с учетом обновленного системного промпта
      String json = fetchValidJson(userMsg, Prompts.CONTENT_SYSTEM_PROMPT, 3);
      String sanitizedJson = jsonUtils.cleanJsonResponse(json);

      ContentResponse response = objectMapper.readValue(sanitizedJson, ContentResponse.class);

      if (response == null || response.getModule() == null) {
        log.error("ИИ вернул пустой модуль для Checkpoint {}", checkpointId);
        return;
      }

      Module module = contentMapper.toEntity(response.getModule());
      module.setCheckpoint(checkpoint);
      checkpoint.setModule(module);

      checkpointRepository.save(checkpoint);
      log.info("<== [SUCCESS] Адаптивный контент для '{}' сохранен", checkpoint.getTitle());

    } catch (Exception e) {
      log.error("!!! [ERROR] Ошибка генерации адаптивного контента: {}", e.getMessage());
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
  @Transactional(readOnly = true)
  public ModuleResponse getModuleByCheckpointId(Long checkpointId) {
    return checkpointRepository.findById(checkpointId)
      .map(Checkpoint::getModule)
      .map(roadmapMapper::toModuleResponse)
      .orElse(null);
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
    String json = jsonUtils.cleanJsonResponse(fetchValidJson(userRequest, Prompts.DEEPEN_TOPIC_SYSTEM_PROMPT, 2));

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

      String cleaned = jsonUtils.cleanJsonResponse(lastResponse);

      if (jsonUtils.isValidJson(cleaned)) {
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