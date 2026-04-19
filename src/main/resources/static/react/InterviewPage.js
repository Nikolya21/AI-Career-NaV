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
        const init = async () => {
            if (vacancyNow && vacancyNow !== "undefined") {
                await detectLanguage();
                await loadFirstQuestion();
            }
        };
        init();
    }, [vacancyNow]);

    const detectLanguage = async () => {
        try {
            const res = await fetch(`/api/v1/compiler/detect?vacancy=${encodeURIComponent(vacancyNow)}`);
            const lang = await res.text();
            const cleanLang = lang.trim().toLowerCase();
            setActiveLanguage(cleanLang);

            const templates = {
                java: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello World!");\n    }\n}',
                python: 'print("Hello World!")',
                javascript: 'console.log("Hello World!");',
                cpp: '#include <iostream>\nint main() {\n    std::cout << "Hello World!";\n    return 0;\n}'
            };
            setCode(templates[cleanLang] || '// Write your code here...');
        } catch (e) {
            setActiveLanguage('none');
        }
    };

    const loadFirstQuestion = async () => {
        try {
            setIsLoading(true);
            await fetch(`/api/v1/quiz/generate/${userId}`, { method: 'POST' });
            const response = await fetch(`/api/v1/quiz/start/${userId}`);
            if (!response.ok) throw new Error("Quiz not found");
            const data = await response.json();
            setCurrentQuestion(data);
            setCurrentNumber(data.number || 1);
        } catch (error) {
            console.error("Error loading question:", error);
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
            setCurrentQuestion(nextData);
            setCurrentNumber(nextData.number);
            setAnswer('');
            setCodeOutput('');
        } catch (err) {
            window.location.href = `/dialogs/chat?userId=${userId}&type=ROADMAP`;
        } finally {
            setIsLoading(false);
        }
    };

    const handleRunCode = async () => {
        setCodeOutput("> Running compilation...");
        try {
            const res = await fetch('/api/v1/compiler/execute', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code, vacancy: vacancyNow })
            });
            const result = await res.json();
            if (result.isTimeout) setCodeOutput("⚠️ Timeout: execution exceeded 15s");
            else setCodeOutput(result.stderr ? `❌ Error:\n${result.stderr}` : `✅ Output:\n${result.stdout}`);
        } catch (err) {
            setCodeOutput("❌ Server error");
        }
    };

    if (isLoading) return <div className="loader-container"><div className="loader"></div><p>Preparing Interview...</p></div>;

    return (
        <div className={`interview-layout ${isCompilerVisible ? 'with-compiler' : 'standard-view'}`}>
            <div className="interview-main">
                <div className="interview-header">
                    <div className="brand">AI Career Nav <span className="badge">Beta</span></div>
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
                            This is a coding task. Please use the IDE on the right to implement your solution in <strong>{activeLanguage.toUpperCase()}</strong>.
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
                                <span className="dot red"></span>
                                <span className="dot yellow"></span>
                                <span className="dot green"></span>
                            </div>
                            <div className="tab active">solution.{activeLanguage === 'python' ? 'py' : activeLanguage === 'java' ? 'java' : 'js'}</div>
                            <button className="run-button" onClick={handleRunCode}>
                                <span className="play-icon">▶</span> RUN
                            </button>
                        </div>
                        <div className="editor-wrapper">
                             <textarea
                                 className="code-editor"
                                 value={code}
                                 onChange={(e) => setCode(e.target.value)}
                                 spellCheck="false"
                             />
                        </div>
                        <div className="terminal">
                            <div className="terminal-header">Terminal</div>
                            <pre className="console-output">{codeOutput || "$ Ready for execution..."}</pre>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
window.InterviewPage = InterviewPage;