package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.user.LoginRequestDto;
import org.example.aicareernav1.dto.user.UserRegistrationDto;
import org.example.aicareernav1.service.user.UserService;
import org.example.aicareernav1.service.user.model.AuthenticationResult;
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

      LoginRequestDto loginRequest = new LoginRequestDto();
      loginRequest.setEmail(registrationDto.getEmail());
      loginRequest.setPassword(registrationDto.getPassword());

      AuthenticationResult authResult = userService.authenticateUser(loginRequest);

      if (authResult.isSuccess()) {
        Long userId = authResult.getUser().getId();
        String userName = authResult.getUser().getName();

        session.setAttribute("user", authResult.getUser());
        session.setAttribute("userEmail", loginRequest.getEmail());
        session.setAttribute("authenticated", true);
        session.setAttribute("userName", userName);
        session.setAttribute("userId", userId);
        session.setAttribute("registrationDate", authResult.getUser().getCreatedAt());

        log.info("✅ Auto-login successful for user: {}", registrationDto.getEmail());

        return "redirect:/personal-cabinet";
      } else {
        log.error("❌ Auto-login failed after registration for {}: {}",
            registrationDto.getEmail(), authResult.getErrors());
        model.addAttribute("errors", authResult.getErrors());
        model.addAttribute("email", registrationDto.getEmail());
        return "login";
      }
    } else {
      for (String error : registrationResult.getErrors()) {
        bindingResult.reject("registration.error", error);
      }
      log.error("Registration failed for email: {}. Errors: {}",
          registrationDto.getEmail(), registrationResult.getErrors());
      return "register";
    }
  }
}