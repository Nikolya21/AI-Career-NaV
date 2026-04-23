const { useState, useEffect } = React;

const InterviewPage = ({ userId, vacancyNow }) => {
    const [currentQuestion, setCurrentQuestion] = useState(null);
    const [currentNumber, setCurrentNumber] = useState(1);
    const [answer, setAnswer] = useState('');
    const [activeLanguage, setActiveLanguage] = useState('none');
    const [code, setCode] = useState('');
    const [codeOutput, setCodeOutput] = useState('');
    const [isLoading, setIsLoading] = useState(true);

    const isCompilerVisible = activeLanguage !== 'none' &&
        (currentQuestion?.compilerRequired === true || String(currentQuestion?.compilerRequired) === 'true');

    useEffect(() => {
        console.log("🛠 [INIT] Запуск инициализации. UserId:", userId, "Vacancy:", vacancyNow);
        const init = async () => {
            if (vacancyNow && vacancyNow !== "undefined") {
                await detectLanguage();
                await loadFirstQuestion();
            } else {
                console.warn("⚠️ [INIT] vacancyNow не определен, ожидание...");
            }
        };
        init();
    }, [vacancyNow]);

    const detectLanguage = async () => {
        try {
            console.log("🔍 [Compiler] Определение языка для:", vacancyNow);
            const res = await fetch(`/api/v1/compiler/detect?vacancy=${encodeURIComponent(vacancyNow)}`);
            const lang = await res.text();
            const cleanLang = lang.trim().toLowerCase();
            console.log("✅ [Compiler] Определен язык:", cleanLang);
            setActiveLanguage(cleanLang);

            const templates = {
                java: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello World!");\n    }\n}',
                python: 'print("Hello World!")',
                javascript: 'console.log("Hello World!");',
                cpp: '#include <iostream>\nint main() {\n    std::cout << "Hello World!";\n    return 0;\n}'
            };
            setCode(templates[cleanLang] || '// Write your code here...');
        } catch (e) {
            console.error("❌ [Compiler] Ошибка определения языка:", e);
            setActiveLanguage('none');
        }
    };

    const loadFirstQuestion = async () => {
        try {
            setIsLoading(true);
            console.log("🆕 [Quiz] Генерация квиза для пользователя...");
            const genRes = await fetch(`/api/v1/quiz/generate/${userId}`, { method: 'POST' });
            console.log("📡 [Quiz] POST /generate статус:", genRes.status);

            const response = await fetch(`/api/v1/quiz/start/${userId}`);
            console.log("📡 [Quiz] GET /start статус:", response.status);

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Ошибка загрузки квиза: ${response.status}. Тело: ${errorText}`);
            }

            const data = await response.json();
            console.log("📝 [Quiz] Первый вопрос получен:", data);
            setCurrentQuestion(data);
            setCurrentNumber(data.number || 1);
        } catch (error) {
            console.error("❌ [Quiz] Ошибка при загрузке вопроса:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleNext = async () => {
        try {
            setIsLoading(true);
            console.log(`➡️ [Step ${currentNumber}] Отправка ответа...`);

            const response = await fetch(`/api/v1/quiz/answer/${userId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    question: currentQuestion.question,
                    answer: isCompilerVisible ? code : answer
                })
            });

            console.log("📡 [Answer] Статус ответа сервера:", response.status);

            if (response.status === 204) {
                console.log("🏁 [Finish] Тест окончен. Инициация финализации...");

                // Использование твоего нового Java-метода (анализ + генерация в одном флаконе)
                console.log("📡 [Finalize] Вызов finalize-and-generate...");
                const finalRes = await fetch(`/api/v1/quiz/${userId}/finalize-and-generate`, {
                    method: 'POST'
                });

                console.log("📡 [Finalize] Статус генерации:", finalRes.status);

                if (finalRes.ok) {
                    const roadmapData = await finalRes.json();
                    console.log("✅ [Success] Roadmap создан:", roadmapData);
                    // Перенаправление на страницу по ID из ответа
                    window.location.href = `/roadmap/${roadmapData.id}`;
                } else {
                    const errDetail = await finalRes.text();
                    console.error("❌ [Error] Сбой финализации:", errDetail);
                    throw new Error("Ошибка при генерации финальной карты");
                }
                return;
            }

            const nextData = await response.json();
            console.log("📥 [Next] Получен следующий вопрос:", nextData);
            setCurrentQuestion(nextData);
            setCurrentNumber(nextData.number);
            setAnswer('');
            setCodeOutput(''); // Очищаем консоль для новой задачи
        } catch (err) {
            console.error("❌ [Critical Error] Ошибка в handleNext:", err);
            // Если критическая ошибка, можно раскомментировать редирект:
            // window.location.href = `/personal-cabinet`;
        } finally {
            setIsLoading(false);
        }
    };

    const handleRunCode = async () => {
        console.log("🚀 [Compiler] Запуск кода...");
        setCodeOutput("> Running compilation...");
        try {
            const res = await fetch('/api/v1/compiler/execute', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code, vacancy: vacancyNow })
            });
            const result = await res.json();
            console.log("💻 [Compiler] Результат выполнения:", result);

            if (result.isTimeout) setCodeOutput("⚠️ Timeout: execution exceeded 15s");
            else setCodeOutput(result.stderr ? `❌ Error:\n${result.stderr}` : `✅ Output:\n${result.stdout}`);
        } catch (err) {
            console.error("❌ [Compiler] Ошибка запроса:", err);
            setCodeOutput("❌ Server error");
        }
    };

    // Отрисовка (оставлена без изменений)
    if (isLoading) return <div className="loader-container"><div className="loader"></div><p>Processing request...</p></div>;

    return (
        <div className={`interview-layout ${isCompilerVisible ? 'with-compiler' : 'standard-view'}`}>
            {/* Твой JSX код без изменений */}
            <div className="interview-main">
                <div className="interview-header">
                    <div className="brand">AI Career Navigator</div>
                    <div className="progress-info">Step {currentNumber} of 12</div>
                </div>
                <div className="progress-container">
                    <div className="progress-fill" style={{ width: `${(currentNumber / 12) * 100}%` }} />
                </div>
                <div className="question-card">
                    <h2 className="question-title">{currentQuestion?.question}</h2>
                    {!isCompilerVisible ? (
                        <textarea
                            className="answer-input"
                            value={answer}
                            onChange={(e) => setAnswer(e.target.value)}
                            placeholder="Type your detailed answer here..."
                        />
                    ) : (
                        <div className="compiler-hint">
                            <span className="hint-icon">💡</span>
                            Coding task. Use the IDE in <strong>{activeLanguage.toUpperCase()}</strong>.
                        </div>
                    )}
                    <div className="actions">
                        <button className="next-button" onClick={handleNext}>
                            {currentNumber >= 12 ? 'Finish Interview' : 'Next Question →'}
                        </button>
                    </div>
                </div>
            </div>

            {isCompilerVisible && (
                <div className="compiler-section">
                    <div className="ide-window">
                        <div className="ide-header">
                            <div className="mac-dots">
                                <span className="dot red"></span><span className="dot yellow"></span><span className="dot green"></span>
                            </div>
                            <div className="tab active">solution.{activeLanguage === 'python' ? 'py' : activeLanguage === 'java' ? 'java' : 'js'}</div>
                            <button className="run-button" onClick={handleRunCode}>RUN</button>
                        </div>
                        <div className="editor-wrapper">
                             <textarea className="code-editor" value={code} onChange={(e) => setCode(e.target.value)} spellCheck="false" />
                        </div>
                        <div className="terminal">
                            <div className="terminal-header">Terminal</div>
                            <pre className="console-output">{codeOutput || "$ Ready..."}</pre>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
window.InterviewPage = InterviewPage;