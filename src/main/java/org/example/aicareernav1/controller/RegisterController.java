package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
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
public class RegisterController {

  @GetMapping("/register")
  public String showRegisterForm() {
    return "register";
  }

  @PostMapping("/register")
  public String register(
      @RequestParam("name") String name,
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      @RequestParam("confirmPassword") String confirmPassword,
      HttpSession session,
      Model model) throws UnsupportedEncodingException {

    List<String> errors = new ArrayList<>();


    if (name == null || name.trim().isEmpty()) {
      errors.add("Имя не может быть пустым");
    } else if (name.length() < 2) {
      errors.add("Имя должно содержать минимум 2 символа");
    }

    if (email == null || email.trim().isEmpty()) {
      errors.add("Email не может быть пустым");
    } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      errors.add("Введите корректный email");
    }

    if (password == null || password.trim().isEmpty()) {
      errors.add("Пароль не может быть пустым");
    } else if (password.length() < 6) {
      errors.add("Пароль должен содержать минимум 6 символов");
    }

    if (confirmPassword == null || !confirmPassword.equals(password)) {
      errors.add("Пароли не совпадают");
    }


    if (!errors.isEmpty()) {
      model.addAttribute("errors", errors);
      model.addAttribute("name", name);
      model.addAttribute("email", email);
      return "register";
    }

    // TODO: Сохранить пользователя в БД
    // Здесь нужно сохранить пользователя в базу данных
    // Для теста просто сохраняем в сессию

    session.setAttribute("authenticated", true);
    session.setAttribute("userEmail", email);
    session.setAttribute("userName", name);
    session.setAttribute("userId", System.currentTimeMillis());

    log.info("✅ Пользователь зарегистрирован: {} ({})", name, email);

    // Перенаправляем на логин с параметром успеха
    return "redirect:/login?registered=true&email=" + java.net.URLEncoder.encode(email, "UTF-8");
  }
}