package org.example.aicareernav1.dto.roadmap.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.example.aicareernav1.dto.roadmap.ModuleDTO;

@Data
public class ContentResponse {
  @JsonProperty("module")
  private ModuleDTO module;
}
