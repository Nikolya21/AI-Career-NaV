package org.example.aicareernav1.dto.roadmap.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapCardResponse {
  private Long id;
  private String targetJobTitle;
  private String createdAt;
  private Double progress;
  private boolean current;
}

