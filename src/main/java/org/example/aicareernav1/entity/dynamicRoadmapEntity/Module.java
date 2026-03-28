package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import jakarta.persistence.*;

import lombok.Data;


import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Module {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @OneToOne
  @JoinColumn(name = "checkpoint_id")
  private Checkpoint checkpoint;

  // Теперь сабтопик содержит список уроков
  @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Lesson> lessons = new ArrayList<>();

  // в будущем сюда можно будет добавить finalTest и собеседование
}