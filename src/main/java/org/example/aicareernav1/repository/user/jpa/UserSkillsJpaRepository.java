package org.example.aicareernav1.repository.user.jpa;

import org.example.aicareernav1.model.user.entity.UserSkillsEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSkillsJpaRepository extends JpaRepository<UserSkillsEntity, Long> {
  Optional<UserSkillsEntity> findByUser_Id(Long userId);
}