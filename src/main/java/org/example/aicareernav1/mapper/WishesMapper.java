package org.example.aicareernav1.mapper;


import org.example.aicareernav1.dto.wishes.WishesCreateDto;
import org.example.aicareernav1.dto.wishes.WishesResponseDto;
import org.example.aicareernav1.model.user.UserWishes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(
  componentModel = "spring",
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WishesMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  UserWishes toEntity(WishesCreateDto dto);

  WishesResponseDto toResponseDto(UserWishes wishes);

  void updateEntity(WishesCreateDto dto, @MappingTarget UserWishes userWishes);
}
