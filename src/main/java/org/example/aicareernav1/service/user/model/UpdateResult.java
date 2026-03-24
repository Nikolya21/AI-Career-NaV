package org.example.aicareernav1.service.user.model;

import java.util.List;
import java.util.logging.Logger;
import lombok.Getter;

@Getter
public class UpdateResult {
  private final boolean success;
  private final List<String> errors;

  Logger logger = Logger.getLogger(getClass().getName());

  private UpdateResult(boolean success, List<String> errors) {
    this.success = success;
    this.errors = errors;
  }

  public static UpdateResult success() {
    return new UpdateResult(true, List.of());
  }

  public static UpdateResult error(String error) {
    return new UpdateResult(false, List.of(error));
  }

  public void printErrors() {
    if (!success) {
      logger.info("❌ ОШИБКИ ОБНОВЛЕНИЯ:");
      errors.forEach(error -> logger.info("   - " + error));
    }
  }
}