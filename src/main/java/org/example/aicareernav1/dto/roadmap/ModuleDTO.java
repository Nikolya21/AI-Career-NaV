package org.example.aicareernav1.dto.roadmap;

import lombok.Data;
import java.util.List;

@Data
public class ModuleDTO {
  private String title;
  private List<LessonDTO> lessons;
  // потом сюда можно будет вставить собеседование или что-то другое (может тест)
}
