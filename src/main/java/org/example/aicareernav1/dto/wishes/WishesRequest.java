package org.example.aicareernav1.dto.wishes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishesRequest {
  private Long userId;
  private String desiredProfession;
  private String additionalComments;
}
