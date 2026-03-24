package org.example.aicareernav1.dto.roadMapDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.aicareernav1.entity.roadmapEntity.RoadmapEntity;

import java.util.List;

@Data
@AllArgsConstructor
public class RoadMapResponse {
  private String message;
  private List<RoadmapEntity> plan;
}

