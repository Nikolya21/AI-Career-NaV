package org.example.aicareernav1.dto.roadmap;

import lombok.Data;

import java.util.List;

@Data
public class LessonDTO {
  private String title;
  private TheoryDTO theory;
  private List<TaskDTO> tasks;
}
