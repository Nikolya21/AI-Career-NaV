package org.example.aicareernav1.mapper;

import org.example.aicareernav1.dto.external.pythonRAG.SaveRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface RagIntegrationMapper {

    @Mapping(target = "content", source = "theory.text")
    @Mapping(target = "tags", source = "extractedTags")
    @Mapping(target = "contentType", constant = "finished_lesson")
    // Теперь мапим именно очищенный запрос
    @Mapping(target = "query", source = "refinedQuery")
    @Mapping(target = "metadata", expression = "java(generateMetadata(lesson))")
    SaveRequest toSaveRequest(Theory theory, Lesson lesson, List<String> extractedTags, String refinedQuery);

    default Map<String, Object> generateMetadata(Lesson lesson) {
        return Map.of("lesson_id", lesson.getId());
    }
}