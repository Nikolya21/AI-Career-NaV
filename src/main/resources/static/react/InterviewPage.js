const { useState, useEffect } = React;

const InterviewPage = ({ userId }) => {
    const [currentQuestion, setCurrentQuestion] = useState(null);
    const [currentNumber, setCurrentNumber] = useState(1);
    const [answer, setAnswer] = useState('');
    const [code, setCode] = useState('public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello World!");\n    }\n}');
    const [codeOutput, setCodeOutput] = useState('');
    const [isLoading, setIsLoading] = useState(true);

    const isComp = currentQuestion?.compilerRequired === true || String(currentQuestion?.compilerRequired) === 'true';

    useEffect(() => {
        loadFirstQuestion();
    }, []);

    const loadFirstQuestion = async () => {
        try {
            setIsLoading(true);
            // Проверь, что эти эндпоинты существуют в твоем QuizController
            await fetch(`/api/v1/quiz/generate/${userId}`, { method: 'POST' });
            const response = await fetch(`/api/v1/quiz/start/${userId}`);

            if (!response.ok) throw new Error("Ошибка старта квиза");

            const data = await response.json();
            setCurrentQuestion(data);
            setCurrentNumber(data.number || 1);
        } catch (error) {
            console.error('Error:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleNext = async () => {
        try {
            setIsLoading(true); // ВКЛЮЧАЕМ лоадер
            const body = {
                question: currentQuestion.question,
                answer: isComp ? code : answer
            };

            const response = await fetch(`/api/v1/quiz/answer/${userId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            // Если 204 или любая ошибка — считаем, что вопросы кончились
            if (response.status === 204 || !response.ok) {
                window.location.href = `/dialogs/chat?userId=${userId}&type=ROADMAP`;
                return;
            }

            const nextData = await response.json();

            if (!nextData || !nextData.question) {
                window.location.href = `/dialogs/chat?userId=${userId}&type=ROADMAP`;
                return;
            }

            setCurrentQuestion(nextData);
            setCurrentNumber(nextData.number);
            setAnswer('');
            setCodeOutput('');
        } catch (err) {
            console.error("Ошибка при отправке:", err);
            window.location.href = `/dialogs/chat?userId=${userId}&type=ROADMAP`;
        } finally {
            setIsLoading(false); // ВЫКЛЮЧАЕМ лоадер
        }
    };

    const handleRunCode = async () => {
        setCodeOutput("Компиляция и запуск...");
        try {
            const res = await fetch('/api/v1/compiler/execute', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code: code })
            });

            const result = await res.json(); // Парсим JSON от сервера

            if (result.isTimeout) {
                setCodeOutput("⚠️ Превышено время выполнения (10 сек)");
                return;
            }

            // Если есть ошибки компиляции или выполнения
            if (result.stderr && result.stderr.trim() !== "") {
                setCodeOutput(`❌ Ошибка:\n${result.stderr}`);
            }
            // Если всё успешно
            else {
                setCodeOutput(result.stdout || "Программа успешно выполнена (нет вывода)");
            }

        } catch (err) {
            setCodeOutput("❌ Критическая ошибка соединения с сервером");
        }
    };

    if (isLoading) return <div className="loader">Обработка данных...</div>;

    return (
        <div className={`interview-layout ${isComp ? 'with-compiler' : 'standard-view'}`}>
            <div className="interview-main">
                <div className="progress-container">
                    <div className="progress-fill" style={{ width: `${(currentNumber / 12) * 100}%` }} />
                </div>

                <div className="question-card" key={currentNumber}>
                    <h2 className="question-title">{currentQuestion?.question}</h2>

                    {!isComp ? (
                        <textarea
                            className="answer-input"
                            value={answer}
                            onChange={(e) => setAnswer(e.target.value)}
                            placeholder="Введите ваш ответ здесь..."
                        />
                    ) : (
                        <div style={{padding: '20px', background: '#f0f4ff', borderRadius: '8px', color: '#555', marginBottom: '20px'}}>
                            💻 Решите задачу в редакторе справа. Метод main уже создан.
                        </div>
                    )}

                    <button className="next-button" onClick={handleNext}>
                        {currentNumber >= 12 ? 'Завершить сессию' : 'Следующий вопрос →'}
                    </button>
                </div>
            </div>

            {isComp && (
                <div className="compiler-section">
                    <div className="compiler-header">
                        <span>Java 17</span>
                        <button className="run-button" onClick={handleRunCode}>Запустить код</button>
                    </div>
                    <textarea
                        className="code-editor"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        spellCheck="false"
                    />
                    <div className="console-output">
                        <pre>{codeOutput || "Результат выполнения появится здесь..."}</pre>
                    </div>
                </div>
            )}
        </div>
    );
};