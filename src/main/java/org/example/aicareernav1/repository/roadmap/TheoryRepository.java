package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TheoryRepository extends JpaRepository<Theory, Long> {
    // Позволяет найти теорию напрямую через ID урока
    Optional<Theory> findByLessonId(Long lessonId);
}