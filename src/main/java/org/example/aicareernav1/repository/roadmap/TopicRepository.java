package org.example.aicareernav1.repository.roadmap;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
  // Метод для поиска всех блоков конкретного плана по порядку
  List<Topic> findAllByRoadmapIdOrderByOrderIndexAsc(Long roadmapId);
}
