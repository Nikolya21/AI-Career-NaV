package org.example.aicareernav1.dto.questionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class ParsedDataDto {
    private final String text;
    private final String difficulty;
    private final Set<String> tags;
}
