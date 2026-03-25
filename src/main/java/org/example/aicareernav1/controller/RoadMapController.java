package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.roadMapDto.WeekDto;
import org.example.aicareernav1.entity.roadmapEntity.RoadmapWeekEntity;
import org.example.aicareernav1.service.roadMapService.RoadMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roadMap")
@RequiredArgsConstructor
public class RoadMapController {
  private final RoadMapService roadMapService;

  @PostMapping("/generate-Roadmap/{userId}")
  public ResponseEntity<List<RoadmapWeekEntity>> generateRoadMap(@PathVariable Long userId) {
    List<RoadmapWeekEntity> plan = roadMapService.generateAndSaveRoadMap(userId);
    return ResponseEntity.ok(plan);
  }

  // НОВЫЙ МЕТОД: Получить все недели для пользователя
  @GetMapping("/weeks/{userId}")
  public ResponseEntity<List<RoadmapWeekEntity>> getUserWeeks(@PathVariable Long userId) {
    List<RoadmapWeekEntity> weeks = roadMapService.getWeeksByUserId(userId);
    return ResponseEntity.ok(weeks);
  }

  // НОВЫЙ МЕТОД: Получить конкретную неделю с заданиями
  @GetMapping("/week/{weekId}")
  public ResponseEntity<RoadmapWeekEntity> getWeekById(@PathVariable Long weekId) {
    RoadmapWeekEntity week = roadMapService.getWeekById(weekId);
    return ResponseEntity.ok(week);
  }
}