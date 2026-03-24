package org.example.aicareernav1.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {
  @NotBlank(message = "Email обязателен")
  @Email(message = "Некорректный формат email")
  private String email;

  @NotBlank(message = "Пароль обязателен")
  @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
  private String password;

  @NotBlank(message = "Имя обязательно")
  private String name;
}