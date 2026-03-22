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

  private int roadmapId;
  Timestamp createdAt;
  Timestamp updatedAt;
}
