package org.example.aicareernav1.entity.roadmapEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roadmap_tasks", schema = "aicareer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapTaskEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;
  private String taskType;
  private String content;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "week_id")
  @ToString.Exclude
  private RoadmapWeekEntity week;
}
