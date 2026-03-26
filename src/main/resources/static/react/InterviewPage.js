import React, { useState, useEffect } from 'react';
import './InterviewPage.css';

const InterviewPage = ({ userId }) => {
    const [currentQuestion, setCurrentQuestion] = useState(null);
    const [currentNumber, setCurrentNumber] = useState(1);
    const [answer, setAnswer] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [isTransitioning, setIsTransitioning] = useState(false);
    const [isCompleted, setIsCompleted] = useState(false);
    const [totalQuestions, setTotalQuestions] = useState(0);

    useEffect(() => {
        loadFirstQuestion();
    }, []);

    const loadFirstQuestion = async () => {
        try {
            setIsLoading(true);
            // 1. СНАЧАЛА ГЕНЕРИРУЕМ ТЕСТ (POST запрос)
            const questionsResponse = await fetch(`/api/v1/quiz/generate/${userId}`, {
                method: 'POST'
            });

            if (!questionsResponse.ok) throw new Error('Ошибка генерации');

            const questions = await questionsResponse.json();
            setTotalQuestions(questions.length);

            // 2. И ТОЛЬКО ПОТОМ ПОЛУЧАЕМ ПЕРВЫЙ ВОПРОС
            const response = await fetch(`/api/v1/quiz/start/${userId}`);
            const data = await response.json();

            setCurrentQuestion(data);
            setCurrentNumber(data.number);
            setIsLoading(false);

        } catch (error) {
            console.error('Ошибка загрузки:', error);
            setIsLoading(false);
        }
    };

    const saveAnswer = async () => {
        if (!answer.trim()) {
            alert('Пожалуйста, введите ответ');
            return;
        }

        try {
            const response = await fetch(`/api/v1/quiz/answer/${userId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    question: currentQuestion.question,
                    answer: answer.trim()
                }),
            });

// Проверяем, есть ли содержимое в ответе
            const text = await response.text();
            const nextQuestion = text ? JSON.parse(text) : null;

            setIsTransitioning(true);
            setAnswer('');

            setTimeout(() => {
                if (nextQuestion && nextQuestion.question) {
                    setCurrentQuestion(nextQuestion);
                    setCurrentNumber(nextQuestion.number);
                    setIsTransitioning(false);
                } else {
                    // ЕСЛИ ВОПРОСОВ БОЛЬШЕ НЕТ:
                    setIsCompleted(true);
                    // Редирект на роадмап через 3 секунды
                    setTimeout(() => {
                        window.location.href = '/roadmap';
                    }, 3000);
                }
            }, 500);

        } catch (error) {
            console.error('Ошибка сохранения ответа:', error);
            alert('Произошла ошибка при сохранении ответа');
            setIsTransitioning(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            saveAnswer();
        }
    };

    if (isLoading) {
        return (
            <div className="interview-container">
                <div className="loader">Загрузка вопросов...</div>
            </div>
        );
    }

    if (isCompleted) {
        return (
            <div className="interview-container">
                <div className="completion-card">
                    <h2>🎉 Тест успешно завершен!</h2>
                    <p>Спасибо за ваши ответы!</p>
                </div>
            </div>
        );
    }

    return (
        <div className="interview-container">
            <div className="progress-bar">
                <div
                    className="progress-fill"
                    style={{ width: `${(currentNumber / totalQuestions) * 100}%` }}
                />
                <span className="progress-text">
          Вопрос {currentNumber} из {totalQuestions}
        </span>
            </div>

            <div className={`question-card ${isTransitioning ? 'fade-out' : 'fade-in'}`}>
                <h2 className="question-title">{currentQuestion?.question}</h2>

                <div className="answer-section">
          <textarea
              className="answer-input"
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Введите ваш ответ здесь..."
              rows={6}
              disabled={isTransitioning}
              autoFocus
          />

                    <button
                        className="next-button"
                        onClick={saveAnswer}
                        disabled={isTransitioning}
                    >
                        {currentNumber === totalQuestions ? 'Завершить' : 'Следующий вопрос →'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default InterviewPage;