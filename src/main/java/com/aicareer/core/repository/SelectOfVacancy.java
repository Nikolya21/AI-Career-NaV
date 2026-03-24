package com.aicareer.core.repository;

import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import java.util.List;

public interface SelectOfVacancy {
  String analyzeUserPreference(UserPreferences infoAboutPerson);

  List<String> extractThreeVacancies(String gigachatAnswer, int count);

  SelectedPotentialVacancy choosenVacansy(List<String> listPotentialVacancy);

  String formingByParsing(SelectedPotentialVacancy selectedVacancy);

  FinalVacancyRequirements formingFinalVacancyRequirements(String newPromt);

}