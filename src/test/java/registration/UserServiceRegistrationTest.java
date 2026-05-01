package registration;

import org.aspectj.lang.annotation.Before;
import org.example.aicareernav1.dto.user.UserRegistrationDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.user.jpa.UserJpaRepository;
import org.example.aicareernav1.service.user.impl.UserServiceImpl;
import org.example.aicareernav1.service.user.model.RegistrationResult;
import org.example.aicareernav1.validator.user.RegistrationValidator; // Импорт валидатора
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic; // Для статики
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceRegistrationTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto dto;

    @BeforeEach
    void setUp() {
        dto = new UserRegistrationDto();
    }

    @Test
    void registerUser_ShouldReturnSuccess_WhenDataIsValid() {
        // 1. Данные
        dto.setName("Ivan");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        // 2. Мокаем статический метод валидатора
        try (MockedStatic<RegistrationValidator> mockedValidator = mockStatic(RegistrationValidator.class)) {
            // Заставляем валидатор вернуть пустой список ошибок
            mockedValidator.when(() -> RegistrationValidator.validate(any(), any()))
              .thenReturn(Collections.emptyList());

            // 3. Мокаем репозиторий
            UserEntity savedEntity = UserEntity.builder()
              .id(1L)
              .email("test@example.com")
              .build();
            when(userJpaRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

            // 4. Вызов
            RegistrationResult result = userService.registerUser(dto);

            // 5. Проверка
            assertTrue(result.isSuccess(), "Регистрация должна быть успешной");
            assertNotNull(result.getUser());
            verify(userJpaRepository, times(1)).save(any());
        }
    }

    @Test
    void registerUser_ShouldReturnError_WhenEmailIsAlreadyTaken() {
        // 1. Подготовка данных
        dto.setEmail("existing@example.com");
        dto.setName("Ivan");
        dto.setPassword("password123");

        // 2. Мокаем статический валидатор так, будто он нашел ошибку
        try (MockedStatic<RegistrationValidator> mockedValidator = mockStatic(RegistrationValidator.class)) {
            mockedValidator.when(() -> RegistrationValidator.validate(any(), any()))
              .thenReturn(List.of("Пользователь с таким email уже существует"));

            // 3. Вызываем метод
            RegistrationResult result = userService.registerUser(dto);

            // 4. Проверяем, что регистрация НЕ прошла
            assertFalse(result.isSuccess(), "Регистрация должна провалиться, если email занят");
            assertEquals(1, result.getErrors().size());
            assertEquals("Пользователь с таким email уже существует", result.getErrors().get(0));

            // Проверяем, что метод save в базу данных ДАЖЕ НЕ ВЫЗЫВАЛСЯ
            verify(userJpaRepository, never()).save(any(UserEntity.class));
        }
    }
}