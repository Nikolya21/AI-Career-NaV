package emailTest;

import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.email.AnalysisMailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalysisMailServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AnalysisMailService analysisMailService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setEmail("test@mail.ru");
        testUser.setTestAnalysis("Ваш идеальный путь — Java Developer.");
    }

    @Test
    void shouldSendEmailSuccess() {
        when(userRepository.findByEmail("test@mail.ru")).thenReturn(Optional.of(testUser));
        analysisMailService.sendAnalysisByEmail("test@mail.ru");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldNotSendEmailIfAnalysisIsEmpty() {
        testUser.setTestAnalysis(null);
        when(userRepository.findByEmail("test@mail.ru")).thenReturn(Optional.of(testUser));
        analysisMailService.sendAnalysisByEmail("test@mail.ru");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldHandleUserNotFound() {
        when(userRepository.findByEmail("unknown@mail.ru")).thenReturn(Optional.empty());
        analysisMailService.sendAnalysisByEmail("unknown@mail.ru");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
