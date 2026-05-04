package org.example.aicareernav1.dto.roadmap.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.aicareernav1.enums.CheckpointStatus;
import lombok.Builder;
import lombok.Data;
import org.example.aicareernav1.enums.CheckpointType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointResponse {
  private Long id;
  private String title;
  private String description;
  private CheckpointStatus status;
  private CheckpointType type;
  private Integer orderIndex;
  private Long parentCheckpointId;
  private Long sourceLessonId;
  private Long roadmapId;
  private ModuleResponse module;
  private Integer totalLessons;
  private Integer completedLessons;
}