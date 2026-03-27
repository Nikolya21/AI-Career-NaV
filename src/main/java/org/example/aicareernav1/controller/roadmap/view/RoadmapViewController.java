package org.example.aicareernav1.controller.roadmap.view;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/roadmap")
@RequiredArgsConstructor
public class RoadmapViewController {

  private final RoadmapService roadmapService; // Только сервис!

  @GetMapping("/{id}")
  public String viewRoadmap(@PathVariable Long id, Model model) {
    // Логика получения данных теперь скрыта в сервисе
    Roadmap roadmap = roadmapService.getRoadmapWithTopics(id);
    double progress = roadmapService.calculateProgress(id);

    model.addAttribute("roadmap", roadmap);
    model.addAttribute("progress", progress);
    return "roadmap/main";
  }

  @GetMapping("/checkpoint/{checkpointId}/details")
  public String getCheckpointDetails(@PathVariable Long checkpointId, Model model) {
    model.addAttribute("checkpoint", roadmapService.getCheckpoint(checkpointId));
    return "roadmap/fragments :: checkpoint-details";
  }
}