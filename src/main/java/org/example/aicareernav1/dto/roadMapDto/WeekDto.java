package org.example.aicareernav1.dto.roadMapDto;

import lombok.Data;

import java.util.List;

@Data
public class WeekDto {
  private int weekNumber;
  private String weekTopic; // Тема недели
  private List<TaskDto> tasks; // Список из 5 объектов-задач
}
