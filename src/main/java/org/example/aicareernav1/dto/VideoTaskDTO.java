package org.example.aicareernav1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskDTO {
  private String taskId;
  private String topic;
  private String status;
  private String videoUrl;
}