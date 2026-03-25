
package org.example.aicareernav1.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.user.UserPreferences;
import org.example.aicareernav1.model.vacancy.PotentialVacancy;
import org.example.aicareernav1.model.vacancy.SelectedPotentialVacancy;
import org.example.aicareernav1.service.parser.SelectVacancy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/choose-vacancy")
@RequiredArgsConstructor
public class ChooseVacancyController {

  private final SelectVacancy selectVacancy;

  @GetMapping
  public String showChooseVacancy(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    UserPreferences userPreferences = (UserPreferences) session.getAttribute("userPreferences");

    if (userPreferences != null) {
      try {
        String analysisResult = selectVacancy.analyzeUserPreference(userPreferences);
        List<String> threeVacancies = selectVacancy.extractThreeVacancies(analysisResult, 0);

        session.setAttribute("suggestedVacancies", threeVacancies);
        session.setAttribute("analysisResult", analysisResult);
        model.addAttribute("analysisResult", analysisResult);
        model.addAttribute("suggestedVacancies", threeVacancies);
      } catch (Exception e) {
        log.error("Ошибка при анализе вакансий", e);
        model.addAttribute("error", "Ошибка при подборе вакансий: " + e.getMessage());
        showTestVacancies(model);
      }
    } else {
      model.addAttribute("error", "Данные пользователя не найдены. Пройдите диалог заново.");
      showTestVacancies(model);
    }

    return "ChooseVacancy";
  }

  @PostMapping
  public String chooseVacancy(@RequestParam("selectedVacancy") String selectedVacancyName,
      HttpSession session,
      Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    if (selectedVacancyName == null || selectedVacancyName.trim().isEmpty()) {
      model.addAttribute("error", "Пожалуйста, выберите вакансию");
      restoreVacanciesFromSession(session, model);
      return "ChooseVacancy";
    }

    try {
      List<String> suggestedVacancies = (List<String>) session.getAttribute("suggestedVacancies");

      if (suggestedVacancies == null || !suggestedVacancies.contains(selectedVacancyName)) {
        model.addAttribute("error", "Выбрана недопустимая вакансия");
        restoreVacanciesFromSession(session, model);
        return "ChooseVacancy";
      }

      PotentialVacancy potentialVacancy = new PotentialVacancy();
      potentialVacancy.setNameOfVacancy(selectedVacancyName);
      SelectedPotentialVacancy selectedVacancy = new SelectedPotentialVacancy(potentialVacancy);

      session.setAttribute("selectedVacancy", selectedVacancy);
      session.setAttribute("selectedVacancyName", selectedVacancyName);

      log.info("✅ Пользователь выбрал вакансию: {}", selectedVacancyName);

      return "redirect:/real-vacancies";
    } catch (Exception e) {
      log.error("Ошибка при обработке выбора вакансии", e);
      model.addAttribute("error", "Ошибка при обработке выбора: " + e.getMessage());
      restoreVacanciesFromSession(session, model);
      return "ChooseVacancy";
    }
  }

  private void showTestVacancies(Model model) {
    List<String> testVacancies = List.of("Java Developer", "Frontend Developer", "Data Scientist");
    model.addAttribute("suggestedVacancies", testVacancies);
    model.addAttribute("analysisResult", "Тестовый анализ: пользователь подходит для IT-профессий");
  }

  private void restoreVacanciesFromSession(HttpSession session, Model model) {
    List<String> suggestedVacancies = (List<String>) session.getAttribute("suggestedVacancies");
    String analysisResult = (String) session.getAttribute("analysisResult");

    if (suggestedVacancies != null) {
      model.addAttribute("suggestedVacancies", suggestedVacancies);
    }
    if (analysisResult != null) {
      model.addAttribute("analysisResult", analysisResult);
    }
  }
}
