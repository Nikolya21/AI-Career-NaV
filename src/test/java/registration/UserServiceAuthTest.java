package registration;

import org.example.aicareernav1.dto.user.LoginRequestDto;
import org.example.aicareernav1.repository.user.jpa.UserJpaRepository;
import org.example.aicareernav1.service.user.impl.UserServiceImpl;
import org.example.aicareernav1.service.user.model.AuthenticationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void authenticateUser_ShouldReturnError_WhenUserNotFound() {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("wrong@test.com");
        loginRequest.setPassword("any");
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        AuthenticationResult result = userService.authenticateUser(loginRequest);
        assertFalse(result.isSuccess());
        assertEquals("Неверный email или пароль", result.getErrors().get(0));
    }
}