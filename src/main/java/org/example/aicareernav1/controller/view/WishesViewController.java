package org.example.aicareernav1.controller.view;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.service.userService.UserService; // Замените на ваш пакет сервиса
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/wishes")
@RequiredArgsConstructor // Добавьте эту аннотацию, чтобы Spring внедрил UserService
public class WishesViewController {

  private final UserService userService;

  @GetMapping("/form")
  public String getWishesForm(@RequestParam(name = "userId", required = false) Long userId, Model model) {
    System.out.println("DEBUG: Получен userId из URL: " + userId);

    if (userId != null) {
      model.addAttribute("userId", userId);
    }
    return "wishes-page";
  }

}
