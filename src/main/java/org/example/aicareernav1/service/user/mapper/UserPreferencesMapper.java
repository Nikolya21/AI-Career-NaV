package org.example.aicareernav1.service.user.mapper;

import org.example.aicareernav1.model.user.UserPreferences;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.model.user.entity.UserPreferencesEntity;

public class UserPreferencesMapper {
  private UserPreferencesMapper() {
    /* This utility class should not be instantiated */
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }


  public static UserPreferences toModel(UserPreferencesEntity entity) {
    if (entity == null) return null;
    return UserPreferences.builder()
        .id(entity.getId())
        .userId(entity.getUser().getId())
        .infoAboutPerson(entity.getInfoAboutPerson())
        .build();
  }

  public static UserPreferencesEntity toEntity(UserPreferences model, UserEntity userEntity) {
    if (model == null) return null;
    return UserPreferencesEntity.builder()
        .id(model.getId())
        .user(userEntity)
        .infoAboutPerson(model.getInfoAboutPerson())
        .build();
  }
}