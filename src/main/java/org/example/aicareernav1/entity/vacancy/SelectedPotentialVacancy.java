package org.example.aicareernav1.entity.vacancy;

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
