package org.example.aicareernav1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class QuizViewController {

  @GetMapping("/quiz-view/{userId}")
  public String getQuizPage(@PathVariable Long userId, Model model) {
    model.addAttribute("userId", userId);
    return "quiz"; // Будет искать файл src/main/resources/templates/quiz.html
  }
}
