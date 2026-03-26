package org.example.aicareernav1.validator.user;

import java.util.function.Predicate;
import org.example.aicareernav1.dto.user.UserRegistrationDto;
import org.example.aicareernav1.validator.util.ValidationUtil;
import java.util.ArrayList;
import java.util.List;

public class RegistrationValidator {

  private RegistrationValidator() {
    /* This utility class should not be instantiated */
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }


  public static List<String> validate(UserRegistrationDto dto, Predicate<String> emailAvailabilityChecker) {
    List<String> errors = new ArrayList<>();

    try {
      ValidationUtil.validateEmail(dto.getEmail(), "Email");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    try {
      ValidationUtil.validateName(dto.getName(), "Name");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    try {
      ValidationUtil.validatePassword(dto.getPassword(), "Password");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    if (errors.isEmpty() && !emailAvailabilityChecker.test(dto.getEmail())) {
      errors.add("Email already taken");
    }

    return errors;
  }
}