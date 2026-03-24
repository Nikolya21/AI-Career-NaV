package org.example.aicareernav1.repository;

import org.example.aicareernav1.model.user.UserPreferences;
import org.example.aicareernav1.model.vacancy.FinalVacancyRequirements;
import org.example.aicareernav1.model.vacancy.SelectedPotentialVacancy;
import java.util.List;

public interface SelectOfVacancy {
  String analyzeUserPreference(UserPreferences infoAboutPerson);

  List<String> extractThreeVacancies(String gigachatAnswer, int count);

  SelectedPotentialVacancy choosenVacansy(List<String> listPotentialVacancy);

  String formingByParsing(SelectedPotentialVacancy selectedVacancy);

  FinalVacancyRequirements formingFinalVacancyRequirements(String newPromt);

}