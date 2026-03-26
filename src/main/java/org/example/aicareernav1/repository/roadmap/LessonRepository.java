package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
  List<Lesson> findAllByCheckpointId(Long checkpointId);
}