package org.example.aicareernav1.controller;

import org.example.aicareernav1.dto.user.LoginRequestDto;
import org.example.aicareernav1.service.user.UserService;
import org.example.aicareernav1.service.user.model.AuthenticationResult;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

  private final UserService userService;

  @GetMapping("/login")
  public String showLoginForm(@RequestParam(value = "registered", required = false) String registered,
                              @RequestParam(value = "email", required = false) String email,
                              Model model) {
    log.info("=== SHOW LOGIN FORM ===");
    model.addAttribute("loginRequest", new LoginRequestDto());
    if (registered != null) {
      model.addAttribute("registered", true);
      model.addAttribute("registeredEmail", email);
    }
    return "login";
  }

  @PostMapping("/login")
  public String processLogin(@Valid @ModelAttribute LoginRequestDto loginRequest,
                             BindingResult result,
                             HttpSession session,
                             Model model) {

    log.info("=== PROCESS LOGIN ===");
    log.info("Email: {}", loginRequest.getEmail());

    if (result.hasErrors()) {
      log.warn("Validation errors: {}", result.getAllErrors());
      return "login";
    }

    AuthenticationResult authResult = userService.authenticateUser(loginRequest);
    log.info("Authentication result: success={}", authResult.isSuccess());

    if (authResult.isSuccess()) {
      // Получаем ID пользователя из результата аутентификации
      Long userId = authResult.getUser().getId();
      String userName = authResult.getUser().getName();

      log.info("User found - ID: {}, Name: {}, Email: {}", userId, userName, loginRequest.getEmail());

      // Сохраняем все данные в сессию
      session.setAttribute("user", authResult.getUser());
      session.setAttribute("userEmail", loginRequest.getEmail());
      session.setAttribute("authenticated", true);
      session.setAttribute("userName", userName); // Используем имя из БД
      session.setAttribute("userId", userId);
      session.setAttribute("registrationDate", authResult.getUser().getCreatedAt());

      log.info("Session attributes saved:");
      log.info("  - authenticated: {}", session.getAttribute("authenticated"));
      log.info("  - userEmail: {}", session.getAttribute("userEmail"));
      log.info("  - userName: {}", session.getAttribute("userName"));
      log.info("  - userId: {}", session.getAttribute("userId"));

      log.info("✅ Login successful, redirecting to /personal-cabinet");
      return "redirect:/personal-cabinet";
    } else {
      log.warn("❌ Login failed for {}: {}", loginRequest.getEmail(), authResult.getErrors());
      model.addAttribute("errors", authResult.getErrors());
      model.addAttribute("email", loginRequest.getEmail());
      return "login";
    }
  }
}