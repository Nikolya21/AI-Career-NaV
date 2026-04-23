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

    @Builder.Default
    private Double threshold = 0.95;

    @JsonProperty("tag_threshold")
    @Builder.Default
    private Double tagThreshold = 0.3;

    @JsonProperty("tag_weight")
    @Builder.Default
    private Double tagWeight = 2.0;

    @JsonProperty("max_weighted_distance")
    @Builder.Default
    private Double maxWeightedDistance = 0.35;

    @JsonProperty("rerank_threshold")
    @Builder.Default
    private Double rerankThreshold = 0.6;

    @JsonProperty("assemble_article")
    @Builder.Default
    private Boolean assembleArticle = true;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // Список parent_id уроков, которые уже есть в Roadmap
    @Builder.Default
    private List<String> excludedParentIds = new ArrayList<>();

    private Map<String, Object> metadata;
}