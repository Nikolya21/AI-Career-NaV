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
 * Контроллер для визуализации интерфейса дорожной карты на Thymeleaf.
 * Поддерживает интерактивные элементы: формы углубления в тему и блоки обратной связи.
 */
@Controller
@RequestMapping("/roadmap")
@RequiredArgsConstructor
public class RoadmapViewController { //todo: пофиксить логику со статусами Checkpoint, чтобы все корректно отображалось

  private final RoadmapService roadmapService;

  /**
   * Отображает общую структуру дорожной карты (дерево тем).
   * * @param id ID дорожной карты
   * @param model контейнер данных для шаблона
   * @return страница общего вида roadmap/view.html
   */
  @GetMapping("/{id}")
  public String viewRoadmap(@PathVariable Long id, Model model) {
    // Получаем сущность со списком топиков и чекпоинтов
    Roadmap roadmap = roadmapService.getRoadmapWithTopics(id);
    double progress = roadmapService.calculateProgress(id);

    model.addAttribute("roadmap", roadmap);
    model.addAttribute("progress", Math.round(progress));
    model.addAttribute("jobTitle", roadmap.getTargetJobTitle());

    return "roadmap/view";
  }

  /**
   * Отображает страницу конкретного урока с контентом и формами взаимодействия.
   * Именно здесь пользователь видит "плашки" для углубления темы и ввода фидбека.
   *
   * @param checkpointId ID этапа обучения
   * @param model модель для Thymeleaf
   * @return страница урока roadmap/lesson.html
   */
  @GetMapping("/lesson/{checkpointId}")
  public String viewLesson(@PathVariable Long checkpointId, Model model) {
    // 1. Получаем метаданные чекпоинта (заголовок, описание) через DTO
    CheckpointResponse checkpoint = roadmapService.getCheckpointResponse(checkpointId);

    // 2. Получаем сам образовательный контент (уроки, теорию)
    ModuleResponse module = roadmapService.getModuleByCheckpointId(checkpointId);

    // Передаем данные в модель
    model.addAttribute("checkpoint", checkpoint);
    model.addAttribute("module", module);

    // Передаем ID Roadmap для работы формы фидбека (чтобы знать, какой профиль обновлять)
    model.addAttribute("roadmapId", checkpoint.getRoadmapId());

    return "roadmap/lesson";
  }
}