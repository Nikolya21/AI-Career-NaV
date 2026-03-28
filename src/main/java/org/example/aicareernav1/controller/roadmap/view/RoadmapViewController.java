package org.example.aicareernav1.controller.roadmap.view;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.roadmap.response.CheckpointResponse;
import org.example.aicareernav1.dto.roadmap.response.ModuleResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для отображения визуальных страниц дорожной карты.
 * Работает с шаблонами Thymeleaf.
 */
@Controller
@RequestMapping("/roadmap")
@RequiredArgsConstructor
public class RoadmapViewController {

  private final RoadmapService roadmapService;

  /**
   * Отображает главную страницу дорожной карты со всеми темами и этапами.
   *
   * @param id    ID дорожной карты
   * @param model модель для передачи данных в шаблон
   * @return путь к шаблону roadmap-view.html
   */
  @GetMapping("/{id}")
  public String viewRoadmap(@PathVariable Long id, Model model) {
    Roadmap roadmap = roadmapService.getRoadmapWithTopics(id);
    double progress = roadmapService.calculateProgress(id);

    model.addAttribute("roadmap", roadmap);
    model.addAttribute("progress", Math.round(progress));
    model.addAttribute("jobTitle", roadmap.getTargetJobTitle());

    return "roadmap/view"; // Путь к src/main/resources/templates/roadmap/view.html
  }

  /**
   * Отображает страницу конкретного урока внутри чекпоинта.
   *
   * @param checkpointId ID этапа
   * @param model        модель
   * @return путь к шаблону lesson-view.html
   */
  @GetMapping("/lesson/{checkpointId}")
  public String viewLesson(@PathVariable Long checkpointId, Model model) {
    CheckpointResponse checkpoint = roadmapService.getCheckpointResponse(checkpointId);
    ModuleResponse module = roadmapService.getModuleByCheckpointId(checkpointId);

    model.addAttribute("checkpoint", checkpoint);
    model.addAttribute("module", module);

    return "roadmap/lesson";
  }
}