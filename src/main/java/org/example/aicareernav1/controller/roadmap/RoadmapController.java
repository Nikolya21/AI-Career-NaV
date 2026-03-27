package org.example.aicareernav1.controller.roadmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Checkpoint;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

  private final RoadmapService roadmapService;

  /**
   * 1. Создание структуры (скелета) плана
   * На входе: ID заготовки Roadmap и контекст (вакансия/цели)
   */
  @PostMapping("/{id}/skeleton")
  public ResponseEntity<String> createSkeleton(@PathVariable Long id, @RequestBody String userContext) {
    log.info("Запрос на генерацию скелета для Roadmap ID: {}", id);
    roadmapService.createSkeleton(id, userContext);
    return ResponseEntity.ok("Скелет успешно создан и первый шаг активирован");
  }

  /**
   * 2. Генерация контента для конкретного этапа
   * Вызывается, когда пользователь заходит в Checkpoint
   */
  @PostMapping("/checkpoint/{checkpointId}/fill")
  public ResponseEntity<String> fillContent(@PathVariable Long checkpointId) {
    log.info("Запрос на наполнение контентом Checkpoint ID: {}", checkpointId);
    roadmapService.fillCheckpointContent(checkpointId);
    return ResponseEntity.ok("Контент сгенерирован");
  }

  /**
   * 3. Обработка фидбека после прохождения этапа
   */
  @PostMapping("/{id}/feedback")
  public ResponseEntity<String> sendFeedback(@PathVariable Long id, @RequestBody String feedback) {
    log.info("Получен фидбек для Roadmap ID {}: {}", id, feedback);
    roadmapService.processUserFeedback(id, feedback);
    return ResponseEntity.ok("Фидбек учтен, профиль обучения обновлен");
  }

  /**
   * 4. Углубление в тему
   */
  @PostMapping("/checkpoint/{checkpointId}/deepen")
  public ResponseEntity<Checkpoint> deepenTopic(
    @PathVariable Long checkpointId,
    @RequestBody String userRequest) {
    log.info("Запрос на углубление темы для Checkpoint ID: {}", checkpointId);
    Checkpoint newCheckpoint = roadmapService.deepenTopic(checkpointId, userRequest);
    return ResponseEntity.ok(newCheckpoint);
  }
}
