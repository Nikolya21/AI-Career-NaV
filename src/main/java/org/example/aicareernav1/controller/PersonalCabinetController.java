package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.user.User;
import org.example.aicareernav1.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@Slf4j
@Controller
@RequestMapping("/personal-cabinet")
@RequiredArgsConstructor
public class PersonalCabinetController {

  private final UserService userService;

  @GetMapping("/{userId}")
  public String showPersonalCabinet(@PathVariable("userId") Long userId, HttpSession session, Model model) {
    // Проверка авторизации
    if (session.getAttribute("authenticated") == null) {
      log.warn("Доступ запрещен: пользователь не авторизован");
      return "redirect:/login";
    }

    // Для безопасности: проверяем, что ID в URL совпадает с ID в сессии
    Long sessionUserId = (Long) session.getAttribute("userId");
    if (sessionUserId == null || !sessionUserId.equals(userId)) {
      log.error("Попытка доступа к чужому кабинету! Сессия: {}, URL: {}", sessionUserId, userId);
      return "redirect:/login";
    }

    // Данные для отображения (имя берем из сессии)
    User profile = userService.getUserProfile(userId);
    model.addAttribute("userName", session.getAttribute("userName") != null ? session.getAttribute("userName") : profile.getName());
    model.addAttribute("userId", session.getAttribute("userId"));
    model.addAttribute("registrationDate", session.getAttribute("registrationDate"));
    model.addAttribute("currentRoadmapId", profile.getRoadmapId());

    return "personal-cabinet";
  }
}
