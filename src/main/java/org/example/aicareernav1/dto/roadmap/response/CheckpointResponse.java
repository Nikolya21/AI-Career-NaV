package org.example.aicareernav1.dto.roadmap.response;

import org.example.aicareernav1.enums.CheckpointStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckpointResponse {
  private Long id;
  private String title;
  private String description;
  private CheckpointStatus status;
  private Integer orderIndex;
  private Long parentCheckpointId;
}