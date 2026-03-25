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
            const response = await fetch(`/api/v1/quiz/start/${userId}`);
            const data = await response.json();
            setCurrentQuestion(data);
            setCurrentNumber(data.number);
            setIsLoading(false);

            // Получаем общее количество вопросов
            const questionsResponse = await fetch(`/api/v1/quiz/generate/${userId}`, {
                method: 'POST'
            });
            const questions = await questionsResponse.json();
            setTotalQuestions(questions.length);

        } catch (error) {
            console.error('Ошибка загрузки вопросов:', error);
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
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    question: currentQuestion.question,
                    answer: answer.trim()
                }),
            });

            const nextQuestion = await response.json();

            // Анимация перехода
            setIsTransitioning(true);
            setAnswer('');

            setTimeout(() => {
                if (nextQuestion) {
                    setCurrentQuestion(nextQuestion);
                    setCurrentNumber(nextQuestion.number);
                } else {
                    setIsCompleted(true);
                }
                setIsTransitioning(false);
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