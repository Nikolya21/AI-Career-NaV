package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmaps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Roadmap {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId; // Связь с пользователем

  private String targetJobTitle; // Название профессии (например, "Java Developer")

  @Column(columnDefinition = "TEXT")
  private String learningStyleNotes; // Здесь будет лежать что-то вроде: "Предпочитает практику, избегает длинных текстов, любит юмор"

  @Column(columnDefinition = "TEXT")
  private String userContext; // резюме или пожелания (что-то)

  private LocalDateTime createdAt;

  // Связь с блоками (Topic)
  @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @OrderBy("orderIndex ASC")
  private List<Topic> topics = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
