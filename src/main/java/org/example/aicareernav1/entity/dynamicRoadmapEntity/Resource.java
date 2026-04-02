package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.enums.ResourceType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Запрещаем печатать всё подряд
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Сравниваем только по ID
public class Resource {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  private String title; // Название для пользователя
  private String url;

  @Enumerated(EnumType.STRING)
  private ResourceType type; // Видео или Статья

  @ManyToOne
  @JoinColumn(name = "theory_id")
  @JsonBackReference
  private Theory theory;
}
