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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Запрещаем печатать всё подряд
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Сравниваем только по ID
public class Topic {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  private String title; // Например, "Блок 1: Java Core"
  private Integer orderIndex; // Порядок блока в роадмапе

  @ManyToOne
  @JoinColumn(name = "roadmap_id")
  @JsonBackReference // "Обратная" сторона, которую Jackson должен игнорировать
  private Roadmap roadmap;

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
  @OrderBy("orderIndex ASC")
  @JsonManagedReference // "Главная" сторона, которую нужно сериализовать
  private List<Checkpoint> checkpoints = new ArrayList<>();
}