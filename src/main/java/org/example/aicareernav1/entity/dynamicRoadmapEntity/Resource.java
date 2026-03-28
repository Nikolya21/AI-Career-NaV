package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.example.aicareernav1.enums.ResourceType;

@Entity
@Data
@Builder
public class Resource {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title; // Название для пользователя
  private String url;

  @Enumerated(EnumType.STRING)
  private ResourceType type; // Видео или Статья

  @ManyToOne
  @JoinColumn(name = "theory_id")
  private Theory theory;
}
