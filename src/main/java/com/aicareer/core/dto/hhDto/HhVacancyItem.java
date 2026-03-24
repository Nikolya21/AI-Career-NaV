package com.aicareer.core.dto.hhDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HhVacancyItem(String id,
                            String name,
                            HhSalary salary,
                            HhEmployer employer,
                            HhExperience experience,
                            @JsonProperty("key_skills") List<HhKeySkill> keySkills,
                            @JsonProperty("age_restriction") HhAgeRestriction ageRestriction,
                            String description) {

}
