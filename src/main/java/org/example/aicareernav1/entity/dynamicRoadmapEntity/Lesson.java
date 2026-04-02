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
public class Lesson {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  private String title;

  @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference // "Главная" сторона, которую нужно сериализовать
  private Theory theory; // Теперь это объект

  @ManyToOne
  @JoinColumn(name = "module_id")
  @JsonBackReference
  private Module module;

  @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference // "Главная" сторона, которую нужно сериализовать
  private List<Task> tasks = new ArrayList<>();
}