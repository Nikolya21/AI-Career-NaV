package org.example.aicareernav1.controller.roadmap.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/roadmap")
@RequiredArgsConstructor
public class RoadmapViewController {

  private final RoadmapService roadmapService;

  /**
   * Главный экран: только граф.
   * Все данные (узлы, связи, прогресс) подтянутся через JS при загрузке.
   */
  @GetMapping("/{id}")
  public String viewRoadmap(@PathVariable Long id, Model model) {
    log.info("Отображение Roadmap страницы для ID: {}", id);
    Roadmap roadmap = roadmapService.getRoadmapById(id);

    if (roadmap == null) {
      log.error("Roadmap с ID {} не найден", id);
      return "redirect:/error"; // Или создай страницу 404
    }

    model.addAttribute("roadmapId", id);
    model.addAttribute("jobTitle", roadmap.getTargetJobTitle());
    model.addAttribute("progress", 0);

    log.info("Roadmap найден: {}, ID в объекте: {}", roadmap.getTargetJobTitle());
    // Мы передаем только базовые данные, остальное сделает API
    return "roadmap/view";
  }
}