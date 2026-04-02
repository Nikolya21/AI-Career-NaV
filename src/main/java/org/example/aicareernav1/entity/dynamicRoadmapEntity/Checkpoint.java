package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.example.aicareernav1.enums.CheckpointStatus;


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

  @Column(columnDefinition = "TEXT")
  private String description; // Тот текст, который ты написал в скобках (например, "Пойми разницу между stack и heap")

  private Integer orderIndex; // Порядок внутри блока

  @OneToOne(mappedBy = "checkpoint", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference // "Главная" сторона, которую нужно сериализовать
  private Module module;

//  @OneToOne(cascade = CascadeType.ALL) // Связь 1-к-1: у чекпоинта один финальный тест
//  @JoinColumn(name = "test_id") // Создает колонку с ID теста в таблице Checkpoint
//  private Test finalTest;

  @Column(name = "parent_checkpoint_id")
  private Long parentCheckpointId;

  private Integer retryCount = 0; // Обычное поле, Hibernate сам сделает его колонкой
}