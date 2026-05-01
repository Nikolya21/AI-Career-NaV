package roadmapTest;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Checkpoint;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.example.aicareernav1.enums.CheckpointType;
import org.example.aicareernav1.repository.roadmap.CheckpointRepository;
import org.example.aicareernav1.service.roadmap.LessonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonContextTest {

    @Mock
    private CheckpointRepository checkpointRepository;
    @InjectMocks
    private LessonService lessonService;

    @Test
    void collectContext_ShouldConcatenatePreviousSummaries() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);

        Lesson prevLesson = new Lesson();
        prevLesson.setTitle("Basics");
        prevLesson.setTheory(Theory.builder().text("Some text").build());
        prevLesson.setSummary("User knows basics.");

        Checkpoint cp = new Checkpoint();
        Module module = new Module();
        module.setLessons(List.of(prevLesson, lesson));
        cp.setModule(module);
        cp.setParentCheckpoint(Checkpoint.builder().type(CheckpointType.ROOT).build());

        when(checkpointRepository.findByLessonId(1L)).thenReturn(Optional.of(cp));

        String context = lessonService.collectContext(lesson);

        assertTrue(context.contains("User knows basics."));
        assertTrue(context.contains("Basics"));
    }
}