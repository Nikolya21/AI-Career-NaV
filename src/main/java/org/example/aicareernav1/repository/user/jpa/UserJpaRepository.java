package org.example.aicareernav1.repository.user.jpa;

import org.example.aicareernav1.model.user.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);
  boolean existsByEmail(String email);

  /**
   * Поиск пользователя по идентификатору дорожной карты.
   * Возвращает Optional, так как пользователя с таким roadmapId может не существовать.
   */
  Optional<UserEntity> findByRoadmapId(Long roadmapId);
}