package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/personal-cabinet")
@RequiredArgsConstructor
public class PersonalCabinetController {

  @GetMapping
  public String showPersonalCabinet(HttpSession session, Model model) {
    // Проверяем, авторизован ли пользователь
    if (session.getAttribute("authenticated") == null) {
      log.warn("Попытка доступа к личному кабинету без авторизации");
      return "redirect:/login";
    }

    // Данные уже есть в сессии, просто передаём их в модель
    String userEmail = (String) session.getAttribute("userEmail");
    String userName = (String) session.getAttribute("userName");
    Long userId = (Long) session.getAttribute("userId");

    model.addAttribute("userEmail", userEmail);
    model.addAttribute("userName", userName);
    model.addAttribute("userId", userId);

    log.info("✅ Открыт личный кабинет пользователя: {}", userEmail);

    return "personal-cabinet";
  }
}