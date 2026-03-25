package org.example.aicareernav1.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.roadmap.Roadmap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/career-roadmap")
@RequiredArgsConstructor
public class CareerRoadmapController {

  @GetMapping
  public String showRoadmap(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    Roadmap roadmap = (Roadmap) session.getAttribute("generatedRoadmap");
    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
    String personalizedPlan = (String) session.getAttribute("personalizedVacancyPlan");

    if (roadmap == null) {
      model.addAttribute("error", "Карьерный план еще не создан. Пройдите диалог обсуждения вакансии.");
      return "CareerRoadmap";
    }

    model.addAttribute("roadmap", roadmap);
    model.addAttribute("selectedVacancy", selectedVacancy);
    model.addAttribute("personalizedPlan", personalizedPlan);

    return "CareerRoadmap";
  }
}