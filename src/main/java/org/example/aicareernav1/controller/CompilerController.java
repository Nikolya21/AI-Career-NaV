package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.testDto.CodeExecutionRequest;
import org.example.aicareernav1.dto.testDto.CodeExecutionResult;
import org.example.aicareernav1.service.testService.CodeExecutionService;
import org.example.aicareernav1.service.testService.LanguageDetectionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compiler")
@RequiredArgsConstructor
public class CompilerController {

    private final CodeExecutionService executionService;
    private final LanguageDetectionService detectionService;

    @GetMapping("/detect")
    public String detect(@RequestParam String vacancy) {
        return detectionService.detectLanguage(vacancy);
    }

    @PostMapping("/execute")
    public CodeExecutionResult runCode(@RequestBody CodeExecutionRequest request) {
        String lang = detectionService.detectLanguage(request.getVacancy());
        return executionService.execute(request.getCode(), lang);
    }
}