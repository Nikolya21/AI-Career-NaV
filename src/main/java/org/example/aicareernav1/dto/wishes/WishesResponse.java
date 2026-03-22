package org.example.aicareernav1.dto.wishes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishesResponse {
  private boolean success;
  private String message;
  private String nextStepUrl;
}
