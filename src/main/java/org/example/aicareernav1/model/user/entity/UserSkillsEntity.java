package org.example.aicareernav1.model.user.entity;

import org.example.aicareernav1.model.user.converter.SkillGapsConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_skills", schema = "aicareer")
public class UserSkillsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserEntity user;

  @Column(name = "full_compliance_percentage")
  private double fullCompliancePercentage;

  @Convert(converter = SkillGapsConverter.class)
  @Column(name = "skill_gaps", columnDefinition = "TEXT")
  private Map<String, Double> skillGaps;

  @Column(name = "calculated_at", nullable = false)
  private Instant calculatedAt;

  @PrePersist
  @PreUpdate
  public void updateTimestamps() {
    calculatedAt = Instant.now();
  }
}