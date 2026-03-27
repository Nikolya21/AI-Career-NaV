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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title; // Например, "Блок 1: Java Core"
  private Integer orderIndex; // Порядок блока в роадмапе

  @ManyToOne
  @JoinColumn(name = "roadmap_id")
  private Roadmap roadmap;

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
  @OrderBy("orderIndex ASC")
  private List<Checkpoint> checkpoints = new ArrayList<>();
}