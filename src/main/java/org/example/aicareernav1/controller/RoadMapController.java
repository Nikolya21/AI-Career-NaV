package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.service.roadMapService.RoadMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roadMap")
@RequiredArgsConstructor
public class RoadMapController {
  private final RoadMapService roadMapService;

  @PostMapping("/generate-Roadmap/{userId}")
  public ResponseEntity<String> generateRoadMap(@PathVariable Long userId) {
    roadMapService.generateAndSaveRoadMap(userId);
    return ResponseEntity.ok("Roadmap успешно сгенерирован и сохранен в БД");
  }
}
