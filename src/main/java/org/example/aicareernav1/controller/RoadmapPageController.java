package org.example.aicareernav1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RoadmapPageController {

  @GetMapping("/roadmap/{userId}")
  public String roadmapPage(@PathVariable("userId") Long userId, Model model) {
    model.addAttribute("userId", userId);
    return "roadmap"; // ищет roadmap.html в папке templates
  }
}