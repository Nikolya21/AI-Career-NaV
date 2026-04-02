package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true) // Запрещаем печатать всё подряд
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Сравниваем только по ID
public class Theory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  @Column(columnDefinition = "TEXT")
  private String text; // Основной Markdown текст

  @OneToMany(mappedBy = "theory", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @JsonManagedReference
  private List<Resource> resources = new ArrayList<>();

  @OneToOne
  @JoinColumn(name = "lesson_id")
  @JsonBackReference // "Обратная" сторона, которую Jackson должен игнорировать
  private Lesson lesson;
}
