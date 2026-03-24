package org.example.aicareernav1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.wishes.WishesCreateDto;
import org.example.aicareernav1.dto.wishes.WishesResponseDto;
import org.example.aicareernav1.service.wishes.WishesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/wishes")
@RequiredArgsConstructor
public class WishesController {

  private final WishesService wishesService;

  /**
   * Создать пожелания
   * POST /api/wishes?userId=1
   */
  @PostMapping
  public ResponseEntity<WishesResponseDto> createWishes(
    @RequestParam Long userId,
    @Valid @RequestBody WishesCreateDto wishesCreateDto) {

    log.debug("Создание пожеланий для пользователя {}: {}", userId, wishesCreateDto.getWishesMessage());

    WishesResponseDto response = wishesService.saveWishes(userId, wishesCreateDto);

    return ResponseEntity.status(response.getId() != null ? 201 : 200).body(response);
  }

  /**
   * Получить пожелания по ID пользователя
   * GET /api/wishes?userId=1
   */
  @GetMapping
  public ResponseEntity<WishesResponseDto> getWishesByUserId(@RequestParam Long userId) {

    log.debug("Получение пожеланий для пользователя {}", userId);

    WishesResponseDto response = wishesService.getWishesByUserId(userId);
    return ResponseEntity.ok(response);
  }

  /**
   * Получить пожелания по ID пожелания
   * GET /api/wishes/{id}?userId=1
   */
  @GetMapping("/{id}")
  public ResponseEntity<WishesResponseDto> getWishesById(
    @PathVariable Long id,
    @RequestParam Long userId) {

    log.debug("Получение пожеланий ID = {} для пользователя {}", id, userId);

    try {
      WishesResponseDto response = wishesService.getWishesById(id, userId);
      return ResponseEntity.ok(response);
    } catch (SecurityException e) {
      return ResponseEntity.status(403).body(WishesResponseDto.builder()
        .wishesMessage(e.getMessage())
        .build());
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Обновить пожелания
   * PUT /api/wishes/{id}?userId=1
   */
  @PutMapping("/{id}")
  public ResponseEntity<WishesResponseDto> updateWishes(
    @PathVariable Long id,
    @RequestParam Long userId,
    @Valid @RequestBody WishesCreateDto wishesCreateDto) {

    log.debug("Обновление пожеланий ID = {} для пользователя {}", id, userId);

    try {
      WishesResponseDto response = wishesService.updateWishes(id, userId, wishesCreateDto);
      return ResponseEntity.ok(response);
    } catch (SecurityException e) {
      return ResponseEntity.status(403).body(WishesResponseDto.builder()
        .wishesMessage(e.getMessage())
        .build());
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Удалить пожелания по ID
   * DELETE /api/wishes/{id}?userId=1
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWishes(
    @PathVariable Long id,
    @RequestParam Long userId) {

    log.debug("Удаление пожеланий ID = {} для пользователя {}", id, userId);

    try {
      wishesService.deleteWishesById(id, userId);
      return ResponseEntity.noContent().build();
    } catch (SecurityException e) {
      return ResponseEntity.status(403).build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Удалить все пожелания пользователя
   * DELETE /api/wishes?userId=1
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteWishesByUser(@RequestParam Long userId) {

    log.debug("Удаление всех пожеланий для пользователя {}", userId);

    wishesService.deleteWishesByUserId(userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Получение формы для ввода пожеланий (для отображения страницы)
   * GET /api/wishes/form
   */
  @GetMapping("/form")
  public ResponseEntity<WishesResponseDto> getWishesForm() {
    return ResponseEntity.ok(WishesResponseDto.builder()
      .wishesMessage("Форма для заполнения пожеланий")
      .nextStepUrl("/wishes/form.html")
      .build());
  }
}