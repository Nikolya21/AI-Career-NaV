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
public class Module {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  private String title;

  @OneToOne
  @JoinColumn(name = "checkpoint_id")
  @JsonBackReference // "Обратная" сторона, которую Jackson должен игнорировать
  private Checkpoint checkpoint;

  // Теперь сабтопик содержит список уроков
  @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference // "Главная" сторона, которую нужно сериализовать
  private List<Lesson> lessons = new ArrayList<>();

  // в будущем сюда можно будет добавить finalTest и собеседование
}