package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.testDto.CodeExecutionRequest;
import org.example.aicareernav1.dto.testDto.CodeExecutionResult;
import org.example.aicareernav1.service.testService.CodeExecutionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compiler")
@RequiredArgsConstructor
public class CompilerController {

    private final CodeExecutionService executionService;

    @PostMapping("/execute")
    public CodeExecutionResult runCode(@RequestBody CodeExecutionRequest request) {
        return executionService.executeJavaCode(request.getCode());
    }
}