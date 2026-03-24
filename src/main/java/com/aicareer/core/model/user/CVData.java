package com.aicareer.core.model.user;

import java.io.File;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVData {
  private Long id;
  private Long userId;
  private File file;
  private String information; // текст, извлеченный из file(он может быть только в формате PDF или DOCX)

  private Instant uploadedAt;

  public void updateTimestamps() {
      uploadedAt = Instant.now();
  }
}
