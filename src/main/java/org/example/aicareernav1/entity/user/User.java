package org.example.aicareernav1.entity.user;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// основная сущность пользователя
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private Long id;
  private String name;
  private String email;
  private String passwordHash;
  private String vacancyNow; // - delete
  private Long roadmapsId; //
  private Instant createdAt;
  private Instant updatedAt;

  public void updateTimestamps() {
    updatedAt = Instant.now();
  }
}
