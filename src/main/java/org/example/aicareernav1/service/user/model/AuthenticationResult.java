package org.example.aicareernav1.service.user.model;

import lombok.Getter;
import org.example.aicareernav1.model.user.User;
import java.util.List;

@Getter
public class AuthenticationResult {
  private final boolean success;
  private final User user;
  private final List<String> errors;

  private AuthenticationResult(boolean success, User user, List<String> errors) {
    this.success = success;
    this.user = user;
    this.errors = errors;
  }

  public static AuthenticationResult success(User user) {
    return new AuthenticationResult(true, user, List.of());
  }

  public static AuthenticationResult error(List<String> errors) {
    return new AuthenticationResult(false, null, errors);
  }

  public static AuthenticationResult error(String error) {
    return new AuthenticationResult(false, null, List.of(error));
  }

}
