package org.example.aicareernav1.dto.roadmap;

import lombok.Data;

import java.util.List;

@Data
public class LessonDTO {
  private String title;
  private List<TaskDTO> tasks;
}
