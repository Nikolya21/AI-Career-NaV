package com.aicareer.core.model.vacancy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SelectedPotentialVacancy {
  private String nameOfVacancy;
  public SelectedPotentialVacancy(PotentialVacancy nameOfVacancy){
    this.nameOfVacancy = nameOfVacancy.getNameOfVacancy();
  }

  public String getNameOfVacancy() {
    return nameOfVacancy;
  }
}
