package com.aicareer.core.model.vacancy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PotentialVacancy {
  private String nameOfVacancy;
  private String salary;
  private String infAboutVacancy;

  public String getVacancyRequirements() {
    return infAboutVacancy;
  }
  @Override
  public String toString(){
    return String.format("%s\nЗарплата: %s\nИнформация по вакансии: %s\n",
        nameOfVacancy, salary != null ? salary : "не указана", infAboutVacancy
    );
  }
}
