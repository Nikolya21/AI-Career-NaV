package org.example.aicareernav1.service.wishes;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.wishes.WishesCreateDto;
import org.example.aicareernav1.dto.wishes.WishesResponseDto;
import org.example.aicareernav1.model.user.UserWishes;
import org.example.aicareernav1.repository.WishesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishesService {

  private final WishesRepository wishesRepository;

  @Transactional
  public WishesResponseDto saveWishes(Long userId, WishesCreateDto dto) {

    if (wishesRepository.existsByUserId(userId)) {
      return WishesResponseDto.builder()
              .wishesMessage(dto.getWishesMessage())
              .nextStepUrl("/wishes/update")
              .build();
    }

    UserWishes userWishes = UserWishes.builder()
                .userId(userId)
                .wishesMessage(dto.getWishesMessage())
                .build();

    UserWishes saved = wishesRepository.save(userWishes);

    log.info("Сохранены пожелания для пользователя {}: ID = {}", userId, saved.getId());

    return WishesResponseDto.builder()
            .id(saved.getId())
            .wishesMessage(saved.getWishesMessage())
            .nextStepUrl("/vacancies/selection")
            .build();
  }

  @Transactional
  public WishesResponseDto updateWishes(Long wishesId, Long userId, WishesCreateDto dto) {

    // Проверка прав доступа
    if (!wishesRepository.existsByIdAndUserId(wishesId, userId)) {
      throw new SecurityException("Нет доступа к этим пожеланиям");
    }

    // Поиск
    UserWishes existingWishes = wishesRepository.findById(wishesId)
      .orElseThrow(() -> new RuntimeException("Пожелания не найдены"));

    // Обновление
    existingWishes.setWishesMessage(dto.getWishesMessage());
    UserWishes updated = wishesRepository.save(existingWishes);

    log.info("Обновлены пожелания ID = {}", updated.getId());

    return WishesResponseDto.builder()
      .id(updated.getId())
      .wishesMessage(updated.getWishesMessage())
      .nextStepUrl("/vacancies/selection")
      .build();
  }

  @Transactional(readOnly = true)
  public WishesResponseDto getWishesByUserId(Long userId) {

    UserWishes userWishes = wishesRepository.findByUserId(userId)
      .orElse(null);

    if (userWishes == null) {
      return WishesResponseDto.builder()
        .wishesMessage("Пожелания не найдены")
        .nextStepUrl("/wishes/form")
        .build();
    }

    return WishesResponseDto.builder()
      .id(userWishes.getId())
      .wishesMessage(userWishes.getWishesMessage())
      .nextStepUrl("/vacancies/selection")
      .build();
  }

  @Transactional(readOnly = true)
  public WishesResponseDto getWishesById(Long wishesId, Long userId) {

    // Проверка прав доступа
    if (!wishesRepository.existsByIdAndUserId(wishesId, userId)) {
      throw new SecurityException("Нет доступа к этим пожеланиям");
    }

    UserWishes userWishes = wishesRepository.findById(wishesId)
      .orElseThrow(() -> new RuntimeException("Пожелания не найдены"));

    return WishesResponseDto.builder()
      .id(userWishes.getId())
      .wishesMessage(userWishes.getWishesMessage())
      .nextStepUrl("/vacancies/selection")
      .build();
  }

  @Transactional
  public void deleteWishesById(Long wishesId, Long userId) {

    // Проверка прав доступа
    if (!wishesRepository.existsByIdAndUserId(wishesId, userId)) {
      throw new SecurityException("Нет доступа к этим пожеланиям");
    }

    wishesRepository.deleteById(wishesId);
    log.info("Удалены пожелания ID = {}", wishesId);
  }

  @Transactional
  public void deleteWishesByUserId(Long userId) {
    wishesRepository.deleteByUserId(userId);
    log.info("Удалены пожелания для пользователя ID = {}", userId);
  }
}