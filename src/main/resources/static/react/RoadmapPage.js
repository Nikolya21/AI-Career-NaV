const { useState, useEffect } = React;

function RoadmapPage() {
    const [weeks, setWeeks] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedWeek, setSelectedWeek] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedTask, setSelectedTask] = useState(null);
    const userId = window.ROADMAP_USER_ID;

    useEffect(() => {
        loadRoadmap();
    }, []);

    const loadRoadmap = async () => {
        try {
            const response = await fetch(`/roadMap/weeks/${userId}`);
            if (response.ok) {
                const data = await response.json();
                if (data && data.length > 0) {
                    setWeeks(data);
                    setIsLoading(false);
                    return;
                }
            }

            const generateResponse = await fetch(`/roadMap/generate-Roadmap/${userId}`, {
                method: 'POST'
            });
            const generatedWeeks = await generateResponse.json();
            setWeeks(generatedWeeks);
        } catch (error) {
            console.error('Ошибка загрузки роадмапа:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const openWeekModal = (week) => {
        setSelectedWeek(week);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setSelectedWeek(null);
        setSelectedTask(null);
    };

    const openTaskModal = (task) => {
        setSelectedTask(task);
    };

    const closeTaskModal = () => {
        setSelectedTask(null);
    };

    if (isLoading) {
        return React.createElement('div', { className: 'roadmap-container' },
            React.createElement('div', { className: 'loader' }, 'Загрузка вашего плана обучения...')
        );
    }

    // Создаем лестницу с неделями
    const weeksStaircase = React.createElement('div', { className: 'staircase-container' },
        weeks.map((week, index) => {
            const isUnlocked = true;
            const weekClass = `stair-step ${isUnlocked ? 'unlocked' : 'locked'}`;
            const stepNumber = index + 1;

            // Сдвиг для лестницы (каждый следующий шаг правее)
            const stepStyle = {
                marginLeft: `${index * 40}px`,
                zIndex: weeks.length - index
            };

            return React.createElement('div', {
                    key: week.id || week.weekNumber,
                    className: weekClass,
                    style: stepStyle,
                    onClick: () => openWeekModal(week)
                },
                // Соединительная линия между шагами
                index < weeks.length - 1 && React.createElement('div', {
                    className: 'step-connector',
                    style: {
                        left: '50%',
                        top: '-30px',
                        position: 'absolute'
                    }
                }),

                // Кружок с номером недели
                React.createElement('div', { className: 'step-circle' },
                    !isUnlocked && React.createElement('div', { className: 'lock-overlay' }, '🔒'),
                    React.createElement('div', { className: 'step-number' }, week.weekNumber)
                ),

                // Информация о неделе
                React.createElement('div', { className: 'step-info' },
                    React.createElement('h3', { className: 'step-title' }, `Неделя ${week.weekNumber}`),
                    React.createElement('p', { className: 'step-topic' }, week.weekTopic)
                ),

                // Индикатор прогресса
                React.createElement('div', { className: 'step-progress' },
                    React.createElement('div', { className: 'progress-dots' },
                        week.tasks?.map((_, idx) =>
                            React.createElement('span', { key: idx, className: 'dot incomplete' }, '●')
                        )
                    )
                )
            );
        })
    );

    // Модальное окно с заданиями
    const modal = isModalOpen && selectedWeek ? React.createElement('div', {
            className: 'modal-overlay',
            onClick: closeModal
        },
        React.createElement('div', { className: 'modal-content', onClick: (e) => e.stopPropagation() },
            React.createElement('button', { className: 'modal-close', onClick: closeModal }, '×'),
            React.createElement('div', { className: 'modal-icon' },
                React.createElement('img', { src: '/images/photo_egg.jpg', alt: 'egg', className: 'modal-egg-image' })
            ),
            React.createElement('h2', { className: 'modal-title' },
                `Неделя ${selectedWeek.weekNumber}: ${selectedWeek.weekTopic}`
            ),
            React.createElement('div', { className: 'tasks-list' },
                selectedWeek.tasks?.map((task, index) =>
                    React.createElement('div', {
                            key: index,
                            className: 'task-item',
                            onClick: () => openTaskModal(task)
                        },
                        React.createElement('div', { className: 'task-icon' },
                            task.type === 'quiz' && '📝',
                            task.type === 'theory' && '📚',
                            task.type === 'practice' && '💻',
                            !task.type && '📌'
                        ),
                        React.createElement('div', { className: 'task-content' },
                            React.createElement('h4', { className: 'task-title' }, task.title),
                            React.createElement('p', { className: 'task-type' }, task.type || 'Задание')
                        ),
                        React.createElement('div', { className: 'task-arrow' }, '→')
                    )
                )
            )
        )
    ) : null;

    const taskModal = selectedTask ? React.createElement('div', {
            className: 'modal-overlay',
            onClick: closeTaskModal
        },
        React.createElement('div', { className: 'modal-content task-modal', onClick: (e) => e.stopPropagation() },
            React.createElement('button', { className: 'modal-close', onClick: closeTaskModal }, '×'),
            React.createElement('div', { className: 'task-icon-large' },
                selectedTask.type === 'quiz' && '📝',
                selectedTask.type === 'theory' && '📚',
                selectedTask.type === 'practice' && '💻',
                !selectedTask.type && '📌'
            ),
            React.createElement('h2', { className: 'task-modal-title' }, selectedTask.title),
            React.createElement('div', { className: 'task-content-detail' },
                React.createElement('p', null, selectedTask.content)
            ),
            React.createElement('button', { className: 'task-start-button' }, 'Начать задание →')
        )
    ) : null;

    return React.createElement('div', { className: 'roadmap-container' },
        React.createElement('h1', { className: 'roadmap-title' }, 'Ваш путь обучения'),
        React.createElement('p', { className: 'roadmap-subtitle' }, 'Каждая неделя - новый шаг к цели'),
        React.createElement('div', { className: 'staircase-wrapper' },
            weeksStaircase
        ),
        modal,
        taskModal
    );
}

// Рендерим приложение
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(React.createElement(RoadmapPage));