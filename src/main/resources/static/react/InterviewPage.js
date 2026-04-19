const { useState, useEffect } = React;

const InterviewPage = ({ userId, vacancyNow }) => {
    const [currentQuestion, setCurrentQuestion] = useState(null);
    const [currentNumber, setCurrentNumber] = useState(1);
    const [answer, setAnswer] = useState('');
    const [activeLanguage, setActiveLanguage] = useState('none');
    const [code, setCode] = useState('');
    const [codeOutput, setCodeOutput] = useState('');
    const [isLoading, setIsLoading] = useState(true);

    // Вычисляем видимость компилятора
    const isCompilerVisible = activeLanguage !== 'none' &&
        (currentQuestion?.compilerRequired === true || String(currentQuestion?.compilerRequired) === 'true');

    // ЛОГ ДЛЯ ОТЛАДКИ: Срабатывает при каждом изменении состояния
    useEffect(() => {
        if (currentQuestion) {
            console.group("🖥️ Статус компилятора");
            console.log("Вакансия:", vacancyNow);
            console.log("Определенный язык (activeLanguage):", activeLanguage);
            console.log("Вопрос требует компилятор (compilerRequired):", currentQuestion?.compilerRequired);
            console.log("Итоговая видимость (isCompilerVisible):", isCompilerVisible);
            console.groupEnd();
        }
    }, [currentQuestion, activeLanguage, isCompilerVisible]);

    useEffect(() => {
        const init = async () => {
            // Проверяем, что вакансия — это реальная строка, а не undefined
            if (vacancyNow && vacancyNow !== "undefined") {
                await detectLanguage();
                await loadFirstQuestion();
            }
        };
        init();
    }, [vacancyNow]); // Эффект перезапустится, когда vacancyNow обновится

    const detectLanguage = async () => {
        console.log("🔍 Запрос на определение языка для:", vacancyNow);
        try {
            const res = await fetch(`/api/v1/compiler/detect?vacancy=${encodeURIComponent(vacancyNow)}`);
            const lang = await res.text();

            // Чистим ответ на случай, если GigaChat прислал лишние пробелы или кавычки
            const cleanLang = lang.trim().toLowerCase();
            console.log("🤖 GigaChat определил язык как:", cleanLang);

            setActiveLanguage(cleanLang);

            const templates = {
                java: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello World!");\n    }\n}',
                python: 'print("Hello World!")',
                javascript: 'console.log("Hello World!");',
                cpp: '#include <iostream>\nint main() {\n    std::cout << "Hello World!";\n    return 0;\n}',
                csharp: 'using System;\nclass Program {\n    static void Main() {\n        Console.WriteLine("Hello World!");\n    }\n}',
                php: '<?php\necho "Hello World!";'
            };
            setCode(templates[cleanLang] || '// Напишите ваш код здесь');
        } catch (e) {
            console.error("❌ Ошибка определения языка:", e);
            setActiveLanguage('none');
        }
    };

    const loadFirstQuestion = async () => {
        try {
            setIsLoading(true);
            // Генерируем квиз, если это первый вход
            await fetch(`/api/v1/quiz/generate/${userId}`, { method: 'POST' });

            const response = await fetch(`/api/v1/quiz/start/${userId}`);
            if (!response.ok) throw new Error("Квиз не найден");

            const data = await response.json();
            console.log("📥 Получен вопрос из БД:", data);
            setCurrentQuestion(data);
            setCurrentNumber(data.number || 1);
        } catch (error) {
            console.error("❌ Ошибка загрузки вопроса:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleNext = async () => {
        try {
            setIsLoading(true);
            const response = await fetch(`/api/v1/quiz/answer/${userId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    question: currentQuestion.question,
                    answer: isCompilerVisible ? code : answer
                })
            });

            if (response.status === 204 || !response.ok) {
                window.location.href = `/dialogs/chat?userId=${userId}&type=ROADMAP`;
                return;
            }

            const nextData = await response.json();
            console.log("📥 Следующий вопрос:", nextData);
            setCurrentQuestion(nextData);
            setCurrentNumber(nextData.number);
            setAnswer('');
            setCodeOutput('');
        } catch (err) {
            console.error("❌ Ошибка перехода:", err);
            window.location.href = `/dialogs/chat?userId=${userId}&type=ROADMAP`;
        } finally {
            setIsLoading(false);
        }
    };

    const handleRunCode = async () => {
        setCodeOutput("Запуск...");
        try {
            const res = await fetch('/api/v1/compiler/execute', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    code: code,
                    vacancy: vacancyNow
                })
            });
            const result = await res.json();
            console.log("🚀 Результат выполнения:", result);

            if (result.isTimeout) setCodeOutput("⚠️ Превышено время (15 сек)");
            else setCodeOutput(result.stderr ? `❌ Ошибка:\n${result.stderr}` : `✅ Результат:\n${result.stdout}`);
        } catch (err) {
            setCodeOutput("❌ Ошибка сервера");
        }
    };

    if (isLoading) return <div className="loader">Загрузка данных интервью...</div>;

    return (
        <div className={`interview-layout ${isCompilerVisible ? 'with-compiler' : 'standard-view'}`}>
            <div className="interview-main">
                <div className="progress-container">
                    <div className="progress-fill" style={{ width: `${(currentNumber / 12) * 100}%` }} />
                </div>

                <div className="question-card">
                    <div className="question-header">
                        <span className="q-number">Вопрос №{currentNumber}</span>
                    </div>
                    <h2 className="question-title">{currentQuestion?.question}</h2>

                    {!isCompilerVisible ? (
                        <textarea
                            className="answer-input"
                            value={answer}
                            onChange={(e) => setAnswer(e.target.value)}
                            placeholder="Напишите развернутый ответ..."
                        />
                    ) : (
                        <div className="compiler-hint">
                            💻 Для этого вопроса доступен компилятор <strong>{activeLanguage.toUpperCase()}</strong>.
                            Решите задачу в окне справа.
                        </div>
                    )}

                    <div className="actions">
                        <button className="next-button" onClick={handleNext}>
                            {currentNumber >= 12 ? 'Завершить интервью' : 'Далее →'}
                        </button>
                    </div>
                </div>
            </div>

            {isCompilerVisible && (
                <div className="compiler-section">
                    <div className="compiler-header">
                        <div className="lang-badge">{activeLanguage.toUpperCase()}</div>
                        <button className="run-button" onClick={handleRunCode}>Запустить код</button>
                    </div>
                    <textarea
                        className="code-editor"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        spellCheck="false"
                    />
                    <div className="console-output">
                        <pre>{codeOutput || "Консоль: результат появится здесь..."}</pre>
                    </div>
                </div>
            )}
        </div>
    );
};
window.InterviewPage = InterviewPage;