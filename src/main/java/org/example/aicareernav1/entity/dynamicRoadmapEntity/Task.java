package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.example.aicareernav1.enums.TaskType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true) // Запрещаем печатать всё подряд
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Сравниваем только по ID
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  private String title;

  @Enumerated(EnumType.STRING)
  private TaskType type;

  @Column(columnDefinition = "TEXT")
  private String content; // Здесь будет храниться JSON с условием задачи и вариантами ответов

  @ManyToOne
  @JoinColumn(name = "lesson_id")
  @JsonBackReference // "Обратная" сторона, которую Jackson должен игнорировать
  private Lesson lesson;

  private boolean isCompleted = false;
}
