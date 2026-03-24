package org.example.aicareernav1.repository.user.jpa;

import org.example.aicareernav1.model.user.entity.UserPreferencesEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesJpaRepository extends JpaRepository<UserPreferencesEntity, Long> {
  Optional<UserPreferencesEntity> findByUser_Id(Long userId);
  boolean existsByUser_Id(Long userId);
}