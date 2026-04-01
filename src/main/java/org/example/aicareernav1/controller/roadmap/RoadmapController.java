package org.example.aicareernav1.controller.roadmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.response.CheckpointResponse;
import org.example.aicareernav1.dto.roadmap.response.ModuleResponse;
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
   * Создает начальную структуру (скелет) дорожной карты.
   * * @param id      идентификатор дорожной карты
   * @param context контекст пользователя (цели, текущие навыки)
   */
  @PostMapping("/{id}/skeleton")
  public ResponseEntity<Void> createSkeleton(@PathVariable Long id, @RequestBody String context) {
    log.info("API: Запрос на создание скелета для Roadmap ID: {}", id);
    roadmapService.createSkeleton(id, context);
    return ResponseEntity.ok().build();
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
    log.info("API: Принудительное заполнение контента для Checkpoint ID: {}", id);
    roadmapService.fillCheckpointContent(id);
    return ResponseEntity.ok().build();
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
}