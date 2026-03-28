package org.example.aicareernav1.dto.roadmap.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LessonResponse {
  private Long id;
  private String title;
  private TheoryResponse theory;
  private List<TaskResponse> tasks;
}