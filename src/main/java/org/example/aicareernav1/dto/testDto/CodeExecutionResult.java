package org.example.aicareernav1.dto.testDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeExecutionResult {
    private String stdout; // Вывод программы
    private String stderr; // Ошибки компиляции или выполнения
    private int exitCode;  // 0 если всё ок
    private boolean isTimeout; // Превышено ли время выполнения
    private String detectedLanguage;
}