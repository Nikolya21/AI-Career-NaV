package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "TEXT")
  private String text; // Основной Markdown текст

  @OneToMany(mappedBy = "theory", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Resource> resources = new ArrayList<>();

  @OneToOne
  @JoinColumn(name = "lesson_id")
  private Lesson lesson;
}
