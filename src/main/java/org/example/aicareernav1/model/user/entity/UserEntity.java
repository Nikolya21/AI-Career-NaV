package org.example.aicareernav1.model.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", schema = "aicareer")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = true)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "vacancy_now")
  private String vacancyNow;

  @Column(name = "test_analysis", columnDefinition = "TEXT")
  private String testAnalysis;

  @Column(name = "vacancy_requirements")
  private String vacancyRequirements;

  @Column(name = "test_result", columnDefinition = "TEXT")
  private String testResult;

  @Column(name = "adaptation_course", columnDefinition = "TEXT")
  private String adaptationCourse;

  @Column(name = "roadmap_id")
  private Long roadmapId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}