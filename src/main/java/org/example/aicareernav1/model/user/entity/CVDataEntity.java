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
@Table(name = "cv_data", schema = "aicareer")
public class CVDataEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserEntity user;

  @Lob
  @Column(name = "information", columnDefinition = "TEXT")
  private String information;

  @Column(name = "uploaded_at", nullable = false)
  private Instant uploadedAt;

  @PrePersist
  @PreUpdate
  public void updateTimestamps() {
    uploadedAt = Instant.now();
  }
}