package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
  List<Lesson> findAllByModuleId(Long moduleId);

  // Либо поиск через ID чекпоинта (Spring Data JPA пройдет по цепочке Lesson -> Module -> Checkpoint)
  List<Lesson> findAllByModuleCheckpointId(Long checkpointId);

  @Query("SELECT l.module.checkpoint.topic.roadmap.learningStyleNotes FROM Lesson l WHERE l.id = :lessonId")
  String findRoadmapNotesByLessonId(@Param("lessonId") Long lessonId);
}