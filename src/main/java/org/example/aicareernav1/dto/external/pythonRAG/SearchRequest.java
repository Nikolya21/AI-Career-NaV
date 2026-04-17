package org.example.aicareernav1.dto.external.pythonRAG;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.*;

// --- Отправка запроса к Python ---

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String query;
    private Double threshold = 0.9;

    @JsonProperty("tag_threshold")
    private Double tagThreshold = 0.3;

    @JsonProperty("tag_weight")
    private Double tagWeight = 2.0;

    @JsonProperty("max_weighted_distance")
    private Double maxWeightedDistance = 0.35;

    @JsonProperty("rerank_threshold")
    private Double rerankThreshold = 0.5;

    @JsonProperty("assemble_article")
    private Boolean assembleArticle = true;

    private List<String> tags = new ArrayList<>();
    private Map<String, Object> metadata;
}