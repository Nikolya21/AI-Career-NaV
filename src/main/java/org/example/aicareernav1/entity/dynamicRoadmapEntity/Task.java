package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.aicareernav1.enums.TaskType;

@Entity
@Data
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @Enumerated(EnumType.STRING)
  private TaskType type;

  @Column(columnDefinition = "TEXT")
  private String content; // Здесь будет храниться JSON с условием задачи и вариантами ответов

  @ManyToOne
  @JoinColumn(name = "lesson_id")
  private Lesson lesson;

  private boolean isCompleted = false;
}
