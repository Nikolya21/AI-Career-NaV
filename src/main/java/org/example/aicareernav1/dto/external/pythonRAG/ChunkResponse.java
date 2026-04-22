package org.example.aicareernav1.dto.external.pythonRAG;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** --- Получение ответа от Python RAG---
            Чанк - 1 шт                **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkResponse {
    private String content;
    private List<String> resources = new ArrayList<>();

    @JsonProperty("bi_encode_score")
    private Double biEncodeScore;

    @JsonProperty("cross_encode_score")
    private Double crossEncodeScore;

    @JsonProperty("parent_id")
    private String parentId;
}
