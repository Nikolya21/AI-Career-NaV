package org.example.aicareernav1.dto.hhDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HhSalary(Integer from,
                       Integer to,
                       String currency) {

}
