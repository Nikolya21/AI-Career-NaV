package scraperTest;

import org.example.aicareernav1.dto.questionDto.ParsedDataDto;
import org.example.aicareernav1.model.dataBaseQuestion.QuestionEntity;
import org.example.aicareernav1.model.dataBaseQuestion.Tag;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.repository.TagRepository;
import org.example.aicareernav1.service.scraper.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private TagRepository tagRepository;

    @InjectMocks private QuestionService questionService;

    @Test
    void saveQuestions_ShouldNotSaveDuplicate() {
        ParsedDataDto dto = new ParsedDataDto("Existing question?", "Junior", Set.of("Java"));
        when(questionRepository.existsByText(dto.getText())).thenReturn(true);
        questionService.saveQuestions(List.of(dto));
        verify(questionRepository, never()).save(any(QuestionEntity.class));
    }

    @Test
    void saveQuestions_ShouldCreateNewTagIfMissing() {
        ParsedDataDto dto = new ParsedDataDto("New question?", "Middle", Set.of("Python"));
        when(questionRepository.existsByText(dto.getText())).thenReturn(false);
        when(tagRepository.findByName("Python")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(new Tag("Python"));
        questionService.saveQuestions(List.of(dto));
        verify(tagRepository).save(argThat(tag -> tag.getName().equals("Python")));
        verify(questionRepository).save(any(QuestionEntity.class));
    }
}