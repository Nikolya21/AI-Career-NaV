package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Test;
import org.example.aicareernav1.enums.CheckpointStatus;

import java.util.ArrayList;
import java.util.List;


@Entity // Помечает класс как таблицу в базе данных
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Checkpoint {

  @Id // Помечает поле как Primary Key (уникальный идентификатор)
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент (1, 2, 3...) в БД
  private Long id;

  @Column(name = "title_name", length = 100) // Настройка обычной колонки (имя в БД, длина)
  private String title;

  @Enumerated(EnumType.STRING) // Храним Enum как текст ("COMPLETED"), а не как число (0)
  private CheckpointStatus status;

  @ManyToOne
  @JoinColumn(name = "topic_id")
  private Topic topic;

  private String description; // Тот текст, который ты написал в скобках (например, "Пойми разницу между stack и heap")
  private Integer orderIndex; // Порядок внутри блока


  // Связь "Один ко многим": один чекпоинт содержит список уроков
  // mappedBy: говорит, что за связь отвечает поле "checkpoint" в классе Lesson
  // cascade: если удалим чекпоинт — удалятся и все его уроки
  // orphanRemoval: если удалить урок из списка lessons, он удалится и из базы данных
  @OneToMany(mappedBy = "checkpoint", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Lesson> lessons = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL) // Связь 1-к-1: у чекпоинта один финальный тест
  @JoinColumn(name = "test_id") // Создает колонку с ID теста в таблице Checkpoint
  private Test finalTest;

  @Column(name = "parent_checkpoint_id")
  private Long parentCheckpointId;

  private Integer retryCount = 0; // Обычное поле, Hibernate сам сделает его колонкой
}