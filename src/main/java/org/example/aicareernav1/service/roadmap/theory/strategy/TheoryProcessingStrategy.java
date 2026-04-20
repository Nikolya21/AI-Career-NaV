package org.example.aicareernav1.service.roadmap.theory.strategy;

import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;


public interface TheoryProcessingStrategy {
    boolean supports(String status);
    Theory process(GatewayResponse response, SearchRequest originalRequest, Lesson lesson, RoadmapConfig config);
}