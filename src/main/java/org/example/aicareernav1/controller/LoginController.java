package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

  @GetMapping("/login")
  public String showLoginForm() {
    return "login";
  }

  @PostMapping("/login")
  public String login(
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      HttpSession session,
      Model model) {

    List<String> errors = new ArrayList<>();

    // Валидация
    if (email == null || email.trim().isEmpty()) {
      errors.add("Email не может быть пустым");
    }

    if (password == null || password.trim().isEmpty()) {
      errors.add("Пароль не может быть пустым");
    }

    if (!errors.isEmpty()) {
      model.addAttribute("errors", errors);
      model.addAttribute("email", email);
      return "login";
    }

    // TODO: Проверить пользователя в БД
    // Для теста просто проверяем, что email и пароль не пустые
    // В реальном приложении нужно проверять в базе данных

    if (password.length() >= 6) {
      session.setAttribute("authenticated", true);
      session.setAttribute("userEmail", email);
      session.setAttribute("userName", email.split("@")[0]);

      log.info("✅ Пользователь вошел: {}", email);

      return "redirect:/personal-cabinet";
    } else {
      errors.add("Неверный email или пароль");
      model.addAttribute("errors", errors);
      model.addAttribute("email", email);
      return "login";
    }
  }

  @GetMapping("/logout")
  public String logout(HttpSession session) {
    session.invalidate();
    log.info("👋 Пользователь вышел");
    return "redirect:/login";
  }
}