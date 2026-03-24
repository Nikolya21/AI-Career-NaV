package org.example.aicareernav1.entity.roadmapEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;

@Entity
@Table(name = "roadmap", schema = "aicareer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoadmapEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "week_number")
  private Integer weekNumber;

  @Column(name = "field_1")
  private String field1;

  @Column(name = "field_2")
  private String field2;

  @Column(name = "field_3")
  private String field3;

  @Column(name = "field_4")
  private String field4;

  @Column(name = "field_5")
  private String field5;
}
