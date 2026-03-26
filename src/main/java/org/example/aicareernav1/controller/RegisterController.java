package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.user.UserRegistrationDto;
import org.example.aicareernav1.service.user.UserService;
import org.example.aicareernav1.service.user.model.RegistrationResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegisterController {

  private final UserService userService;

  // ВАЖНО: Добавляем метод для отображения формы регистрации
  @GetMapping("/register")
  public String showRegisterForm(Model model) {
    log.info("=== SHOW REGISTER FORM ===");
    model.addAttribute("userRegistrationDto", new UserRegistrationDto());
    return "register";
  }

  @PostMapping("/register")
  public String register(
    @Valid @ModelAttribute("userRegistrationDto") UserRegistrationDto registrationDto,
    BindingResult bindingResult,
    HttpSession session,
    Model model) throws UnsupportedEncodingException {

    log.info("Processing registration for email: {}", registrationDto.getEmail());

    // 1. Проверяем совпадение паролей
    if (!bindingResult.hasFieldErrors("password") &&
      !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Пароли не совпадают");
    }

    // 2. Проверяем, существует ли email
    if (!bindingResult.hasFieldErrors("email") && !userService.isEmailAvailable(registrationDto.getEmail())) {
      bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
    }

    // 3. Если есть ошибки валидации, возвращаемся на форму
    if (bindingResult.hasErrors()) {
      log.warn("Registration validation failed: {}", bindingResult.getAllErrors());
      return "register";
    }

    // 4. Сохраняем пользователя через UserService
    RegistrationResult registrationResult = userService.registerUser(registrationDto);

    if (registrationResult.isSuccess()) {
      log.info("✅ User registered successfully: {} ({})",
        registrationDto.getName(), registrationDto.getEmail());

      // Перенаправляем на логин с параметром успеха
      String encodedEmail = URLEncoder.encode(registrationDto.getEmail(), StandardCharsets.UTF_8.toString());
      return "redirect:/login?registered=true&email=" + encodedEmail;
    } else {
      // Если регистрация не удалась, добавляем ошибки в BindingResult
      for (String error : registrationResult.getErrors()) {
        bindingResult.reject("registration.error", error);
      }
      log.error("Registration failed for email: {}. Errors: {}",
        registrationDto.getEmail(), registrationResult.getErrors());
      return "register";
    }
  }
}