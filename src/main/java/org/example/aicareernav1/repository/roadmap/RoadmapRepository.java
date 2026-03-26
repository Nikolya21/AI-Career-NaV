package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

}
