package org.example.aicareernav1.entity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
  private Long id;
  private Long userId;
  private String infoAboutPerson;
}