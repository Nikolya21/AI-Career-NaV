package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.repository.UserRepository; // Импортируем репозиторий
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class QuizViewController {

  // Внедряем интерфейс репозитория напрямую
  private final UserRepository userRepository;

  @GetMapping("/quiz-view/{userId}")
  public String getQuizPage(@PathVariable Long userId, Model model) {
    userRepository.findById(userId).ifPresent(user -> {
      model.addAttribute("userId", userId);
      model.addAttribute("vacancyNow", user.getVacancyNow());
    });

    return "quiz";
  }
}