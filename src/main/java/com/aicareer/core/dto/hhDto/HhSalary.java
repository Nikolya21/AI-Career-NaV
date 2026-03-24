package com.aicareer.core.dto.hhDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HhSalary(Integer from,
                       Integer to,
                       String currency) {

}
