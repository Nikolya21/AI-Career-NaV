package org.example.aicareernav1.dto.roadmap;

import lombok.Data;

import java.util.List;

@Data
public class LessonDTO { //todo: разобраться с Lesson и Task - ошибки в промптах / моделях => Jackson не может нармально распарсить
  private String title;
  private List<TaskDTO> tasks;
}
