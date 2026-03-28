package org.example.aicareernav1.dto.roadmap.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceResponse {
  private String title;
  private String url;
  private String type;
}