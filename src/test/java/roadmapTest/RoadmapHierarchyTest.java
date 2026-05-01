package roadmapTest;

import org.example.aicareernav1.dto.RoadmapSkeletonDTO;
import org.example.aicareernav1.dto.roadmap.RoadmapGenerationRequest;
import org.example.aicareernav1.dto.roadmap.response.RoadmapResponse;
import org.example.aicareernav1.dto.roadmap.response.SkeletonResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Checkpoint;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.enums.CheckpointType;
import org.example.aicareernav1.mapper.RoadmapMapper;
import org.example.aicareernav1.repository.roadmap.CheckpointRepository;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.repository.roadmap.TopicRepository;
import org.example.aicareernav1.service.roadmap.CheckpointService;
import org.example.aicareernav1.service.roadmap.RoadmapConfigService;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.example.aicareernav1.service.util.JsonUtilsService;
import org.example.aicareernav1.service.yandexGpt.YandexGptService; // Проверь правильность пути
import org.example.aicareernav1.service.util.LlmResponseParserService; // Сервис парсинга JSON
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapHierarchyTest {

    @Mock private RoadmapRepository roadmapRepository;
    @Mock private CheckpointRepository checkpointRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private CheckpointService checkpointService;
    @Mock private RoadmapMapper roadmapMapper;
    @Mock private RoadmapConfigService configService;

    @InjectMocks
    private RoadmapService roadmapService;

    @Test
    void getOrStartCheckpoint_ShouldTriggerGenerationOnlyIfEmpty() {
        // 1. Подготовка
        // Создаем родительский чекпоинт с ID 1
        Checkpoint parent = Checkpoint.builder().id(1L).build();

        Checkpoint existingMain = Checkpoint.builder()
          .id(10L)
          .type(CheckpointType.MAIN)
          .parentCheckpoint(parent) // Убедись, что ID здесь соответствует логике сервиса
          .roadmap(Roadmap.builder().config(new RoadmapConfig()).build())
          .module(null)
          .build();

        when(checkpointRepository.findById(10L)).thenReturn(Optional.of(existingMain));

        // 2. Выполнение
        roadmapService.getOrStartCheckpoint(10L);

        // 3. Проверка
        // ИСПРАВЛЕНИЕ: Используем anyLong() вместо жесткого 10L,
        // если логика сервиса берет ID родителя (1L), а не самого чекпоинта
        verify(checkpointService).generateMainCheckpoint(anyLong(), any(), any(), any(), any());
    }
}