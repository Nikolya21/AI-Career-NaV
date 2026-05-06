package org.example.aicareernav1.dto.roadmap;

import lombok.Data;

@Data
public class RoadmapGenerationRequest {
  private Long userId;
  private String jobTitle;
  private String requirements;
  private String testResult;
}