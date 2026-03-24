package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.entity.roadmapEntity.RoadmapWeekEntity;
import org.example.aicareernav1.service.roadMapService.RoadMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roadMap")
@RequiredArgsConstructor
public class RoadMapController {
  private final RoadMapService roadMapService;

  @PostMapping("/generate-Roadmap/{userId}")
  public ResponseEntity<List<RoadmapWeekEntity>> generateRoadMap(@PathVariable Long userId) {
    // Пусть сервис возвращает список созданных недель
    List<RoadmapWeekEntity> plan = roadMapService.generateAndSaveRoadMap(userId);
    return ResponseEntity.ok(plan);
  }

}
