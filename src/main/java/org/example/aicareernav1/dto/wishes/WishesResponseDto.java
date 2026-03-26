package org.example.aicareernav1.dto.wishes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishesResponseDto {
  private Long id;
  private String wishesMessage;
  private String nextStepUrl;
}
