package org.example.aicareernav1.service.Scraper;

import org.example.aicareernav1.dto.questionDto.ParsedDataDto;
import org.example.aicareernav1.model.dataBaseQuestion.Question;
import org.example.aicareernav1.model.dataBaseQuestion.Tag;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.repository.TagRepository; // Тебе тоже нужно будет его создать
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // Говорит Spring, что это класс с бизнес-логикой
public class QuestionService {

    // Spring сам создаст эти объекты и "подложит" их сюда (Dependency Injection)
    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;

    public QuestionService(QuestionRepository questionRepository, TagRepository tagRepository) {
        this.questionRepository = questionRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional // Если что-то пойдет не так, база "откатит" изменения назад
    public void saveQuestions(List<ParsedDataDto> dtos) {
        for (ParsedDataDto dto : dtos) {

            // Проверяем на дубликаты
            if (questionRepository.existsByText(dto.getText())) {
                continue;
            }

            // Создаем сущность вопроса
            Question question = new Question();
            question.setText(dto.getText());
            question.setDifficulty(dto.getDifficulty());

            // Обрабатываем теги из DTO
            for (String tagName : dto.getTags()) {
                // Ищем тег в БД или создаем новый, если такого еще нет
                Tag tag = tagRepository.findByName(tagName)
                  .orElseGet(() -> tagRepository.save(new Tag(tagName)));

                question.getTags().add(tag);
            }

            // Сохраняем готовый вопрос со связями в базу!
            questionRepository.save(question);
        }
    }
}