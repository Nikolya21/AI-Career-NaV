package org.example.aicareernav1.dto.wishes;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishesCreateDto {

  @NotBlank(message = "Если не знаешь, что написать - обратись в чат с ИИ помощником")
  private String wishesMessage;
}
