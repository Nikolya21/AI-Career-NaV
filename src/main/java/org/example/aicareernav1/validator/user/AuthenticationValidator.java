package org.example.aicareernav1.validator.user;

import org.example.aicareernav1.dto.user.LoginRequestDto;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AuthenticationValidator {
  private AuthenticationValidator() {
    /* This utility class should not be instantiated */
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
          "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
  );

  public static List<String> validate(LoginRequestDto dto) {
    List<String> errors = new ArrayList<>();

    String email = dto.getEmail();
    String password = dto.getPassword();

    if (email == null || email.trim().isEmpty()) {
      errors.add("Email не может быть пустым");
    } else {
      email = email.trim();
      if (!EMAIL_PATTERN.matcher(email).matches()) {
        errors.add("Некорректный формат email (должен содержать @ и домен, например: user@example.com)");
      }
    }

    if (password == null || password.trim().isEmpty()) {
      errors.add("Пароль не может быть пустым");
    }

    return errors;
  }
}