package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Checkpoint;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Long> {
    // Найти текущий активный чекпоинт пользователя
    Optional<Checkpoint> findByRoadmapIdAndStatus(Long roadmapId, CheckpointStatus status);
    List<Checkpoint> findAllByTopicIdOrderByOrderIndexAsc(Long topicId);
}

