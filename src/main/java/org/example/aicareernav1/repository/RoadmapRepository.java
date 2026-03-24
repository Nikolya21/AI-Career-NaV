package org.example.aicareernav1.repository;

import org.example.aicareernav1.entity.roadmapEntity.RoadmapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<RoadmapEntity, Long> {
  void deleteByUserId(Long userId);
}
