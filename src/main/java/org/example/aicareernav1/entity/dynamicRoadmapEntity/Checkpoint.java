package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.example.aicareernav1.enums.CheckpointType;

import java.util.ArrayList;
import java.util.List;


@Entity // Помечает класс как таблицу в базе данных
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Запрещаем печатать всё подряд
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Сравниваем только по ID
public class Checkpoint {

  @Id // Помечает поле как Primary Key (уникальный идентификатор)
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент (1, 2, 3...) в БД
  @ToString.Include // Разрешаем печатать ID в логах
  @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
  private Long id;

  @Column(name = "title_name", columnDefinition = "TEXT") // Настройка обычной колонки (имя в БД, длина)
  private String title;

  @Enumerated(EnumType.STRING) // Храним Enum как текст ("COMPLETED"), а не как число (0)
  private CheckpointStatus status;

  @ManyToOne
  @JoinColumn(name = "topic_id")
  @JsonBackReference // "Обратная" сторона, которую Jackson должен игнорировать
  private Topic topic;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "roadmap_id", nullable = false) // Теперь каждый узел знает свою карту
  @JsonBackReference
  private Roadmap roadmap;

  @OneToOne(mappedBy = "checkpoint", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private CheckpointContext context;

  @Column(columnDefinition = "TEXT")
  private String description; // Тот текст, который ты написал в скобках (например, "Пойми разницу между stack и heap")

  private Integer orderIndex; // Порядок внутри блока

  @OneToOne(mappedBy = "checkpoint", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference // "Главная" сторона, которую нужно сериализовать
  private Module module;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_checkpoint_id")
  @JsonBackReference
  private Checkpoint parentCheckpoint;

  @OneToMany(mappedBy = "parentCheckpoint", cascade = CascadeType.ALL)
  @JsonManagedReference
  private List<Checkpoint> children = new ArrayList<>();

  // НОВОЕ: чтобы знать, какой именно урок вызвал ветвление
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_lesson_id")
  private Lesson sourceLesson;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private CheckpointType type = CheckpointType.MAIN;

  @Builder.Default
  private Integer retryCount = 0; // Обычное поле, Hibernate сам сделает его колонкой

  public void addChild(Checkpoint child) {
    this.children.add(child);
    child.setParentCheckpoint(this); // Синхронизируем обе стороны в одном месте
  }
}