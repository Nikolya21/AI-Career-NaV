package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    @Query("SELECT r.config FROM Roadmap r " +
            "JOIN r.topics t " +
            "JOIN t.checkpoints cp " +
            "JOIN cp.modules m " +
            "JOIN m.lessons l " +
            "WHERE l.id = :lessonId")
    Optional<RoadmapConfig> findConfigByLessonId(@Param("lessonId") Long lessonId);

    /**
     * Поиск конфигурации напрямую через ID роадмапа.
     */
    @Query("SELECT r.config FROM Roadmap r WHERE r.id = :roadmapId")
    Optional<RoadmapConfig> findConfigByRoadmapId(@Param("roadmapId") Long roadmapId);
}
