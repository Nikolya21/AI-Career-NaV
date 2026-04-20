package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Checkpoint;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Long> {
    // Найти текущий активный чекпоинт пользователя
    @Query("SELECT cp FROM Checkpoint cp WHERE cp.topic.roadmap.id = :roadmapId AND cp.status = :status")
    Optional<Checkpoint> findByRoadmapIdAndStatus(
      @Param("roadmapId") Long roadmapId,
      @Param("status") CheckpointStatus status
    );

    // В CheckpointRepository.java
    @Query("SELECT c.roadmap FROM Checkpoint c WHERE c.id = :checkpointId")
    Optional<Roadmap> findRoadmapByCheckpointId(@Param("checkpointId") Long checkpointId);

    @Query("SELECT cp FROM Checkpoint cp LEFT JOIN FETCH cp.module WHERE cp.id = :id")
    Optional<Checkpoint> findByIdWithModule(@Param("id") Long id);

    List<Checkpoint> findAllByTopicIdOrderByOrderIndexAsc(Long topicId);
}

