package org.example.aicareernav1.service.user.model;

import java.util.logging.Logger;
import lombok.Getter;
import org.example.aicareernav1.model.user.entity.UserEntity;
import java.util.List;

@Getter
public class RegistrationResult {

  Logger logger = Logger.getLogger(getClass().getName());
  private final boolean success;
  private final UserEntity user;
  private final List<String> errors;

  private RegistrationResult(boolean success, UserEntity user, List<String> errors) {
    this.success = success;
    this.user = user;
    this.errors = errors;
  }

  public static RegistrationResult success(UserEntity user) {
    return new RegistrationResult(true, user, List.of());
  }

  public static RegistrationResult error(List<String> errors) {
    return new RegistrationResult(false, null, errors);
  }

  public void printErrors() {
    if (!success) {
      logger.info("❌ ОШИБКИ РЕГИСТРАЦИИ:");
      errors.forEach(error -> logger.info("   - " + error));
    }
  }
}