package org.example.aicareernav1.service.user.mapper;

import org.example.aicareernav1.model.user.CVData;
import org.example.aicareernav1.model.user.entity.CVDataEntity;
import org.example.aicareernav1.model.user.entity.UserEntity;

public class CVDataMapper {
  private CVDataMapper() {
    /* This utility class should not be instantiated */
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }


  public static CVData toModel(CVDataEntity entity) {
    if (entity == null) return null;
    return CVData.builder()
        .id(entity.getId())
        .userId(entity.getUser().getId())
        .file(null) // или можно создать временный файл, но это затратно
        .information(entity.getInformation())
        .uploadedAt(entity.getUploadedAt())
        .build();
  }

  public static CVDataEntity toEntity(CVData model, UserEntity userEntity) {
    if (model == null) return null;
    return CVDataEntity.builder()
        .id(model.getId())
        .user(userEntity)
        .information(model.getInformation())
        .uploadedAt(model.getUploadedAt())
        .build();
  }
}