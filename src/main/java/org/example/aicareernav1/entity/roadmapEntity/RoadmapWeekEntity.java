package org.example.aicareernav1.entity.roadmapEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "roadmap_weeks", schema = "aicareer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapWeekEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;
  private Integer weekNumber;
  private String weekTopic;

  @JsonManagedReference
  @OneToMany(mappedBy = "week", cascade = CascadeType.ALL)
  private List<RoadmapTaskEntity> tasks;
}

