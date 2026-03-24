package org.example.aicareernav1.validator.util;

import org.example.aicareernav1.validator.user.ValidationException;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ValidationUtil {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+\\.[A-Za-z]{2,3}$");

  private static final Pattern NAME_PATTERN =
      Pattern.compile("^[a-zA-Zа-яА-ЯёЁ\\s-]{2,50}$");

  public static boolean isValidEmail(String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  public static boolean isValidName(String name) {
    return name != null && NAME_PATTERN.matcher(name).matches();
  }

  public static boolean isValidPassword(String password) {
    return password != null &&
        password.length() >= 8 &&
        password.length() <= 100 &&
        containsUpperCase(password) &&
        containsLowerCase(password) &&
        containsDigit(password);
  }

  private static boolean containsUpperCase(String str) {
    return str.chars().anyMatch(Character::isUpperCase);
  }

  private static boolean containsLowerCase(String str) {
    return str.chars().anyMatch(Character::isLowerCase);
  }

  private static boolean containsDigit(String str) {
    return str.chars().anyMatch(Character::isDigit);
  }

  public static void validateEmail(String email, String fieldName) {
    if (email == null || email.trim().isEmpty()) {
      throw new ValidationException(fieldName + " is required");
    }
    if (!isValidEmail(email.trim())) {
      throw new ValidationException("Invalid " + fieldName + " format");
    }
  }

  public static void validateName(String name, String fieldName) {
    if (name == null || name.trim().isEmpty()) {
      throw new ValidationException(fieldName + " is required");
    }
    if (!isValidName(name.trim())) {
      throw new ValidationException(fieldName + " must be 2-50 characters and contain only letters, spaces and hyphens");
    }
  }

  public static void validatePassword(String password, String fieldName) {
    if (password == null || password.isEmpty()) {
      throw new ValidationException(fieldName + " is required");
    }
    if (!isValidPassword(password)) {
      throw new ValidationException(fieldName + " must be 8-100 characters, contain uppercase, lowercase letters and at least one digit");
    }
  }
}