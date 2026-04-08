package org.example.aicareernav1.controller.roadmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.CheckpointStatusDTO;
import org.example.aicareernav1.dto.roadmap.RoadmapGenerationRequest;
import org.example.aicareernav1.dto.roadmap.response.CheckpointResponse;
import org.example.aicareernav1.dto.roadmap.response.ModuleResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Контроллер для управления образовательным процессом дорожной карты.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

  private final RoadmapService roadmapService;

  /**
   * Полная генерация дорожной карты: создание сущности + генерация структуры через ИИ.
   * Возвращает ID созданного Roadmap для последующего редиректа.
   */
  @PostMapping("/generate")
  public ResponseEntity<Long> generateRoadmap(@RequestBody RoadmapGenerationRequest request) {
    log.info("API: Запрос на полную генерацию Roadmap для вакансии: {}", request.getJobTitle());
    Roadmap roadmap = roadmapService.generateFullRoadmap(request);
    return ResponseEntity.ok(roadmap.getId());
  }

  /**
   * Получает контент модуля для чекпоинта.
   * Если контента нет, он будет сгенерирован автоматически.
   */
  @GetMapping("/checkpoint/{id}/content")
  public ResponseEntity<ModuleResponse> getCheckpointContent(@PathVariable Long id) {
    log.info("API: Запрос контента для Checkpoint ID: {}", id);
    ModuleResponse response = roadmapService.getModuleByCheckpointId(id);

    if (response == null) {
      roadmapService.fillCheckpointContent(id);
      response = roadmapService.getModuleByCheckpointId(id);
    }

    if (response == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Не удалось сформировать контент для урока");
    }

    return ResponseEntity.ok(response);
  }

  /**
   * Отправляет отзыв пользователя о пройденном этапе для адаптации будущих модулей.
   *
   * @param id       идентификатор Roadmap
   * @param feedback текст отзыва
   */
  @PostMapping("/{id}/feedback")
  public ResponseEntity<Void> sendFeedback(@PathVariable Long id, @RequestBody String feedback) {
    log.info("API: Получен фидбек для Roadmap ID: {}", id);
    roadmapService.processUserFeedback(id, feedback);
    return ResponseEntity.accepted().build();
  }

  /**
   * Создает новый промежуточный чекпоинт для более глубокого изучения текущей темы.
   * Возвращает CheckpointResponse вместо прямой Entity для безопасности данных.
   *
   * @param id      идентификатор текущего чекпоинта
   * @param request уточняющий запрос пользователя (что именно изучить подробнее)
   * @return DTO созданного чекпоинта
   */
  @PostMapping("/checkpoint/{id}/deepen")
  public ResponseEntity<CheckpointResponse> deepenTopic(@PathVariable Long id, @RequestBody String request) {
    log.info("API: Запрос на углубление темы для Checkpoint ID: {}", id);
    CheckpointResponse response = roadmapService.deepenTopic(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Принудительное обновление (перегенерация) контента чекпоинта.
   */
  @PostMapping("/checkpoint/{id}/fill-content")
  public ResponseEntity<Void> fillContent(@PathVariable Long id) {
    log.info("API: Запрос принят, запускаем фоновую генерацию для ID: {}", id);
    roadmapService.fillCheckpointContent(id);
    return ResponseEntity.accepted().build();
  }

  /**
   * Возвращает текущий прогресс прохождения дорожной карты в процентах.
   *
   * @param id ID дорожной карты
   * @return процент завершения (0.0 - 100.0)
   */
  @GetMapping("/{id}/progress")
  public ResponseEntity<Double> getProgress(@PathVariable Long id) {
    return ResponseEntity.ok(roadmapService.calculateProgress(id));
  }

  /**
   * Возвращает текущий статус этапа обучения (Checkpoint).
   * <p>
   * Метод используется фронтендом для реализации механизма опроса (polling).
   * Это позволяет динамически обновлять интерфейс (например, скрывать лоадер),
   * когда асинхронный процесс генерации контента через ИИ завершен.
   * </p>
   *
   * @param id уникальный идентификатор чекпоинта
   * @return {@link ResponseEntity} содержащая Map со статусом, например: {@code {"status": "ACTIVE"}}
   * @see org.example.aicareernav1.enums.CheckpointStatus
   */
  @GetMapping("/checkpoint/{id}/status")
  public ResponseEntity<CheckpointStatusDTO> getStatus(@PathVariable Long id) {
    CheckpointStatus status = roadmapService.getCheckpointStatus(id);
    return ResponseEntity.ok(new CheckpointStatusDTO(status.name()));
  }
}