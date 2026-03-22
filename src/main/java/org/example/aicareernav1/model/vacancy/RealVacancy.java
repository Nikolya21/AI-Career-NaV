package org.example.aicareernav1.model.vacancy;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealVacancy {
  private String nameOfVacancy;
  private String salary;
  private String experience;
  private String employer;
  private String age;
  private List<String> vacancyRequirements;

  public RealVacancy(String nameOfVacancy, List<String> vacancyRequirements, String salary, String experience, String age, String employer){
    this.nameOfVacancy = nameOfVacancy;
    this.vacancyRequirements = vacancyRequirements;
    this.salary = salary;
    this.experience = experience;
    this.age = age;
    this.employer = employer;
  }

  public String getNameOfVacancy() {
    return nameOfVacancy;
  }

  public String getSalary() {
    return salary;
  }

  public String getExperience() {
    return experience;
  }

  public String getAge() {
    return age;
  }

  public String getEmployer() {
    return employer;
  }

  public List<String> getVacancyRequirements() {
    return vacancyRequirements;
  }

}
