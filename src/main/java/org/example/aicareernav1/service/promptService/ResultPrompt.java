package org.example.aicareernav1.service.promptService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResultPrompt {

  public String buildAnalysisPrompt(Map<String, String> testResults) {
    StringBuilder QAHistory = new StringBuilder();
    testResults.forEach((q, a) -> {
      QAHistory.append("ВОПРОС: ").append(q).append("\n");
      QAHistory.append("ОТВЕТ КАНДИДАТА: ").append(a).append("\n\n");
    });

    return """
            РОЛЬ:
            Ты — Lead Software Engineer и Technical Interviewer. Твоя задача — провести глубокий аудит ответов кандидата.
            
            ВХОДНЫЕ ДАННЫЕ:
            Список вопросов и ответов кандидата:
            %s
            
            ЗАДАЧА:
            1. Проанализируй каждый ответ на полноту, техническую грамотность и понимание архитектурных принципов.
            2. Определи текущий грейд кандидата (Junior, Middle, Senior) на основе глубины его суждений.
            3. Выяви "слепые зоны" (темы, в которых кандидат плавает) и сильные стороны.

            ФОРМАТ ВЫВОДА (JSON):
            {
              "overall_grade": "Junior+/Middle/Senior-",
              "summary": "Общее резюме профессионального профиля (3-4 предложения).",
              "technical_depth_score": "Оценка от 1 до 10",
              "strengths": ["Список сильных сторон"],
              "weak_points": ["Список пробелов в знаниях"],
              "detailed_analysis": [
                {
                  "question": "Текст вопроса",
                  "verdict": "Насколько ответ верен и глубок",
                  "score": "1-5"
                }
              ],
              "recommendations": "Конкретные темы или технологии для изучения."
            }

            КРИТЕРИИ ОЦЕНКИ:
            - Junior: Знает синтаксис, может решить задачу "в лоб", не думает о масштабируемости.
            - Middle: Понимает как работают инструменты под капотом, думает о базах данных, кэшировании и обработке ошибок.
            - Senior: Рассуждает категориями компромиссов (Trade-offs), безопасности, отказоустойчивости и стоимости поддержки решения.

            ОГРАНИЧЕНИЯ:
            - Будь критичен. Не завышай оценку за поверхностные ответы.
            - Весь ответ строго на русском языке в формате JSON.
            """.formatted(QAHistory.toString());
  }
}
