package org.example.aicareernav1.service.user.mapper;

import org.example.aicareernav1.model.user.User;
import org.example.aicareernav1.model.user.entity.UserEntity;

public class UserMapper {
  private UserMapper() {
    /* This utility class should not be instantiated */
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }


  public static User toModel(UserEntity entity) {
    if (entity == null) return null;
    return User.builder()
        .id(entity.getId())
        .name(entity.getName())
        .email(entity.getEmail())
        .passwordHash(entity.getPasswordHash())
        .vacancyNow(entity.getVacancyNow())
        .roadmapId(entity.getRoadmapId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public static UserEntity toEntity(User model) {
    if (model == null) return null;
    return UserEntity.builder()
        .id(model.getId())
        .name(model.getName())
        .email(model.getEmail())
        .passwordHash(model.getPasswordHash())
        .vacancyNow(model.getVacancyNow())
        .roadmapId(model.getRoadmapId())
        .createdAt(model.getCreatedAt())
        .updatedAt(model.getUpdatedAt())
        .build();
  }
}