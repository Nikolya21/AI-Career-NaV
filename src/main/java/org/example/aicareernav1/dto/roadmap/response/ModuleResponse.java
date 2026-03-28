package org.example.aicareernav1.dto.roadmap.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ModuleResponse {
  private Long id;
  private String title;
  private List<LessonResponse> lessons;
}