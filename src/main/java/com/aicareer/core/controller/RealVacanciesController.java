package com.aicareer.core.controller;

import com.aicareer.core.model.vacancy.RealVacancy;
import com.aicareer.core.service.parser.ParserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/real-vacancies")
@RequiredArgsConstructor
public class RealVacanciesController {

  private final ParserService parserService;

  @GetMapping
  public String showRealVacancies(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    String selectedVacancyName = (String) session.getAttribute("selectedVacancyName");

    if (selectedVacancyName != null) {
      try {
        log.info("🔍 Парсим реальные вакансии для: {}", selectedVacancyName);
        List<RealVacancy> realVacancies = parserService.getVacancies(selectedVacancyName, "1", 10);
        log.info("✅ Получено реальных вакансий: {}", realVacancies != null ? realVacancies.size() : 0);

        if (realVacancies != null) {
          realVacancies = decodeVacancies(realVacancies);
        }

        model.addAttribute("realVacancies", realVacancies);
        model.addAttribute("selectedVacancy", selectedVacancyName);
      } catch (Exception e) {
        log.error("Ошибка при парсинге вакансий", e);
        model.addAttribute("error", "Ошибка при загрузке реальных вакансий: " + e.getMessage());
      }
    } else {
      log.warn("❌ Вакансия не выбрана в сессии");
      model.addAttribute("error", "Вакансия не выбрана. Вернитесь к выбору вакансии.");
    }

    return "RealVacancies";
  }

  private List<RealVacancy> decodeVacancies(List<RealVacancy> vacancies) {
    if (vacancies == null) return vacancies;
    for (RealVacancy vacancy : vacancies) {
      if (vacancy.getNameOfVacancy() != null) {
        vacancy.setNameOfVacancy(fixEncoding(vacancy.getNameOfVacancy()));
      }
      if (vacancy.getEmployer() != null) {
        vacancy.setEmployer(fixEncoding(vacancy.getEmployer()));
      }
      if (vacancy.getExperience() != null) {
        vacancy.setExperience(fixEncoding(vacancy.getExperience()));
      }
      if (vacancy.getAge() != null) {
        vacancy.setAge(fixEncoding(vacancy.getAge()));
      }

      if (vacancy.getVacancyRequirements() != null) {
        List<String> decodedSkills = new ArrayList<>();
        for (String skill : vacancy.getVacancyRequirements()) {
          decodedSkills.add(fixEncoding(skill));
        }
        vacancy.setVacancyRequirements(decodedSkills);
      }
    }
    return vacancies;
  }

  private String fixEncoding(String text) {
    if (text == null) return null;
    try {
      if (text.contains("Р") && text.contains("С")) {
        byte[] bytes = text.getBytes("ISO-8859-1");
        return new String(bytes, "UTF-8");
      }
      if (text.contains("&")) {
        text = text.replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&");
      }
      return text;
    } catch (Exception e) {
      log.warn("Ошибка при исправлении кодировки: {}", e.getMessage());
      return text;
    }
  }
}