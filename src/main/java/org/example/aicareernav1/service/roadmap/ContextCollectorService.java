package org.example.aicareernav1.service.roadmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Checkpoint;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.CheckpointContext;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.LessonContext;
import org.example.aicareernav1.service.yandexGpt.YandexGptService;
import org.springframework.stereotype.Service;

import static org.example.aicareernav1.service.roadmap.prompt.ContextCollectorPrompt.getPromptForShortLessonContext;


@Service
@RequiredArgsConstructor
@Slf4j
public class ContextCollectorService {

    private final YandexGptService llmService;


    public String getShortContextFromLesson(Lesson lesson) {
        LessonContext context = LessonContext.builder()
                .lesson(lesson)
                .build();
        String text = lesson.getTheory().getText();
        String shortContext = llmService.sendMessage(getPromptForShortLessonContext(lesson.getTitle(), text));
        context.setShortContext(shortContext);
        lesson.setContext(context);

        return context.getShortContext();
    }

    public String getShortContextFromCheckpoint(Checkpoint checkpoint) {
        StringBuilder contextText = new StringBuilder();

        for (Lesson lesson : checkpoint.getModule().getLessons()) {
            if (lesson.getTheory() != null) {
                contextText.append(lesson.getContext().getShortContext()).append("\n");
            }
        }
        CheckpointContext context;
        if (checkpoint.getContext() == null) {
            context = CheckpointContext.builder()
                    .checkpoint(checkpoint)
                    .shortContext(contextText.toString())
                    .build();
        } else {
            context = checkpoint.getContext();
            context.setShortContext(contextText.toString());
        }

        checkpoint.setContext(context);

        return context.getShortContext();
    }
}
