package com.aicareer.core.dto.hhDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HhEmployer(String name) {

}
