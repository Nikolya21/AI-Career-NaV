package org.example.aicareernav1.repository;

import org.example.aicareernav1.dto.questionDto.ParsedDataDto;

import java.util.List;

public interface ParsingSites {
    List<ParsedDataDto> scrape();
    boolean supports(String siteName);
}
