package org.example.aicareernav1.service.roadmap.theory.strategy;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Resource;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.example.aicareernav1.enums.ResourceType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReadyTheoryStrategy implements TheoryProcessingStrategy {

    @Override
    public boolean supports(String status) {
        return "READY_LESSON".equals(status);
    }

    @Override
    public Theory process(GatewayResponse response, SearchRequest request, Lesson lesson) {
        Theory theory = Theory.builder()
                .text(response.getContent())
                .tags(List.of("FINISH IT"))
                .lesson(lesson)
                .resources(new ArrayList<>())
                .build();

        // Превращаем список строк из Python в ваши сущности Resource
        if (response.getResources() != null) {
            response.getResources().forEach(url -> {
                theory.getResources().add(Resource.builder()
                        .url(url)
                        .title("Справочный материал")
                        .type(ResourceType.ARTICLE)
                        .theory(theory)
                        .build());
            });
        }

        lesson.setTheory(theory);
        return theory;
    }
}
