package org.example.aicareernav1.dto.roadmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlanDTO {
  private String moduleTitle;
  private List<String> lessonOutlines; // Список заголовков подразделов
}
