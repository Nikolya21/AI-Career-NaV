package org.example.aicareernav1.dto.external.pythonRAG;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/** --- Получение ответа от Python RAG---
        Результирующий список чанков - +-5 шт **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GatewayResponse {
    private String status; // READY_LESSON | NEED_GENERATION | NOT_FOUND
    private String content;
    private List<String> resources = new ArrayList<>();
    private List<ChunkResponse> chunks = new ArrayList<>();
}