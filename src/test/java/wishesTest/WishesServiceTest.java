package wishesTest;

import org.example.aicareernav1.dto.wishes.WishesCreateDto;
import org.example.aicareernav1.dto.wishes.WishesResponseDto;
import org.example.aicareernav1.model.user.UserWishes;
import org.example.aicareernav1.repository.WishesRepository;
import org.example.aicareernav1.service.wishes.WishesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishesServiceTest {

    @Mock
    private WishesRepository wishesRepository;

    @InjectMocks
    private WishesService wishesService;

    private Long userId;
    private Long wishesId;
    private WishesCreateDto createDto;
    private UserWishes userWishes;

    @BeforeEach
    void setUp() {
        userId = 1L;
        wishesId = 10L;
        createDto = new WishesCreateDto();
        createDto.setWishesMessage("Хочу работать в BigTech");

        userWishes = UserWishes.builder()
          .id(wishesId)
          .userId(userId)
          .wishesMessage(createDto.getWishesMessage())
          .build();
    }

    @Test
    void saveWishes_ShouldReturnResponseDto() {
        // Arrange
        when(wishesRepository.save(any(UserWishes.class))).thenReturn(userWishes);

        // Act
        WishesResponseDto response = wishesService.saveWishes(userId, createDto);

        // Assert
        assertNotNull(response);
        assertEquals(wishesId, response.getId());
        assertEquals(createDto.getWishesMessage(), response.getWishesMessage());
        assertEquals("/vacancies/selection", response.getNextStepUrl());
        verify(wishesRepository, times(1)).save(any(UserWishes.class));
    }

    @Test
    void updateWishes_Success_ShouldReturnUpdatedDto() {
        // Arrange
        when(wishesRepository.existsByIdAndUserId(wishesId, userId)).thenReturn(true);
        when(wishesRepository.findById(wishesId)).thenReturn(Optional.of(userWishes));
        when(wishesRepository.save(any(UserWishes.class))).thenReturn(userWishes);

        WishesCreateDto updateDto = new WishesCreateDto();
        updateDto.setWishesMessage("Новое пожелание");

        // Act
        WishesResponseDto response = wishesService.updateWishes(wishesId, userId, updateDto);

        // Assert
        assertEquals("Новое пожелание", response.getWishesMessage());
        verify(wishesRepository).save(userWishes);
    }

    @Test
    void updateWishes_AccessDenied_ShouldThrowSecurityException() {
        // Arrange
        when(wishesRepository.existsByIdAndUserId(wishesId, userId)).thenReturn(false);

        // Act & Assert
        assertThrows(SecurityException.class, () ->
          wishesService.updateWishes(wishesId, userId, createDto)
        );
        verify(wishesRepository, never()).save(any());
    }

    @Test
    void getWishesByUserId_WhenNotFound_ShouldReturnFormUrl() {
        // Arrange
        when(wishesRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        WishesResponseDto response = wishesService.getWishesByUserId(userId);

        // Assert
        assertEquals("Пожелания не найдены", response.getWishesMessage());
        assertEquals("/wishes/form", response.getNextStepUrl());
    }

    @Test
    void deleteWishesById_Success_ShouldCallRepository() {
        // Arrange
        when(wishesRepository.existsByIdAndUserId(wishesId, userId)).thenReturn(true);

        // Act
        wishesService.deleteWishesById(wishesId, userId);

        // Assert
        verify(wishesRepository).deleteById(wishesId);
    }

    @Test
    void getWishesById_NotFound_ShouldThrowRuntimeException() {
        // Arrange
        when(wishesRepository.existsByIdAndUserId(wishesId, userId)).thenReturn(true);
        when(wishesRepository.findById(wishesId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
          wishesService.getWishesById(wishesId, userId)
        );
        assertEquals("Пожелания не найдены", exception.getMessage());
    }
}