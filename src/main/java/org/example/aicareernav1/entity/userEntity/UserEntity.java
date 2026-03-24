package org.example.aicareernav1.entity.userEntity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "users", schema = "aicareer")
@Data
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "vacancy_now")
  private String vacancyNow;

  @Column(columnDefinition = "TEXT")
  private String testResult;

  @Column(columnDefinition = "TEXT")
  private String jobRequirements;

  @Column(columnDefinition = "TEXT")
  private String adaptationCourse;

  Timestamp createdAt;
  Timestamp updatedAt;
}
