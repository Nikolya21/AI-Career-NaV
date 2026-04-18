package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Запрещаем печатать всё подряд
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Сравниваем только по ID
public class Roadmap {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  private Long userId; // Связь с пользователем

  private String targetJobTitle; // Название профессии (например, "Java Developer")

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "config_id", referencedColumnName = "id")
  private RoadmapConfig config;

  @Column(columnDefinition = "TEXT")
  private String userContext; // результаты теста + требования к вакансии

  private LocalDateTime createdAt;

  // Связь с блоками (Topic)
  @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @OrderBy("orderIndex ASC")
  @JsonManagedReference // "Главная" сторона, которую нужно сериализовать
  private List<Topic> topics = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
