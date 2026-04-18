package org.example.aicareernav1.service.roadmap;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.response.LessonResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.mapper.ContentMapper;
import org.example.aicareernav1.repository.roadmap.LessonRepository;
import org.example.aicareernav1.service.roadmap.theory.TheoryOrchestrator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final TheoryOrchestrator theoryOrchestrator;
    private final ContentMapper contentMapper;

    @Transactional
    public LessonResponse fillLessonContent(Long lessonId, String userQuery) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        // Делегируем всю сложную логику (RAG, стратегии, LLM) оркестратору
        // Используем название урока в качестве поискового запроса
        theoryOrchestrator.getTheoryForLesson(lessonId, userQuery);

        // Возвращаем обновленный урок через маппер
        return contentMapper.toResponse(lesson);
    }
}
