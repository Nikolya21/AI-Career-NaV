package org.example.aicareernav1.dto.external.pythonRAG;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SaveRequest {
    private String query;
    private String content;

    @JsonProperty("content_type")
    private String contentType = "finished_lesson";

    private List<String> tags;
    private List<String> resources;
    private Map<String, Object> metadata;
}