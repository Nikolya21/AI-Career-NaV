package org.example.aicareernav1.dto.roadmap;

import lombok.Data;

import java.util.List;

@Data
public class ContentResponse {
  private List<LessonDTO> lessons;
}
