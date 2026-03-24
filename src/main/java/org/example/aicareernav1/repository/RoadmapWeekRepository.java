package org.example.aicareernav1.repository;

import org.example.aicareernav1.entity.roadmapEntity.RoadmapWeekEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapWeekRepository extends JpaRepository<RoadmapWeekEntity, Long> {
  void deleteByUserId(Long userId);

  List<RoadmapWeekEntity> findAllByUserIdOrderByWeekNumberAsc(Long userId);
}
