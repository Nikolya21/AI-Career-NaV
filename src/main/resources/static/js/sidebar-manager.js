const sidebarManager = {
    currentCheckpointId: null,
    currentLessonId: null, // Добавляем новое поле
    loadedLessons: [],

    // Открыть/закрыть сайдбар
    toggle(show) {
        const sidebar = document.getElementById('sidebar');
        const headerPanel = document.querySelector('.header-panel');
        const fsBtn = document.getElementById('fullscreen-btn')?.querySelector('.material-icons');

        sidebar.classList.toggle('sidebar-hidden', !show);

        // Если закрываем сайдбар — сбрасываем всё состояние фуллскрина
        if (!show) {
            sidebar.classList.remove('full-viewport');
            document.body.style.overflow = 'auto'; // Возвращаем скролл страницы

            if (headerPanel) {
                headerPanel.style.display = 'flex'; // Гарантированно возвращаем панель
                headerPanel.style.opacity = '1';
            }

            if (fsBtn) {
                fsBtn.innerText = 'open_in_full';
            }

            // Возвращаем стандартную ширину сайдбара
            sidebar.classList.remove('sidebar-expanded');
        }
    },

    // Методы для модального окна фидбека
    openFeedbackModal() {
        document.getElementById('feedback-modal').classList.remove('hidden');
    },

    closeFeedbackModal() {
        document.getElementById('feedback-modal').classList.add('hidden');
        document.getElementById('lesson-feedback-input').value = '';
    },

    // Добавь внутрь объекта sidebarManager:

    toggleFullScreen() {
        const sidebar = document.getElementById('sidebar');
        const fsBtn = document.getElementById('fullscreen-btn').querySelector('.material-icons');
        const headerPanel = document.querySelector('.header-panel'); // Находим прогресс-бар

        const isFull = sidebar.classList.toggle('full-viewport');

        if (isFull) {
            fsBtn.innerText = 'close_fullscreen';
            if (headerPanel) headerPanel.style.opacity = '0'; // Плавно скрываем
            sidebar.classList.add('sidebar-expanded');
        } else {
            fsBtn.innerText = 'open_in_full';
            if (headerPanel) headerPanel.style.opacity = '1'; // Показываем обратно

            // Возвращаем размер в зависимости от того, открыт ли урок
            if (document.getElementById('lesson-list-container').classList.contains('hidden')) {
                 sidebar.classList.add('sidebar-expanded');
            } else {
                 sidebar.classList.remove('sidebar-expanded');
            }
        }
    },

    // 1. Загрузка чекпоинта (список уроков)
    async loadCheckpoint(checkpointId) {
            this.currentCheckpointId = checkpointId;
            this.toggle(true);
            this.showList();

            const listContainer = document.getElementById('lesson-list-container');
            listContainer.innerHTML = '<div class="loader">Связываемся с базой...</div>';

            try {
                const response = await fetch(`/api/v1/roadmap/checkpoint/${checkpointId}`);

                if (!response.ok) {
                    throw new Error("Ошибка сервера при загрузке чекпоинта");
                }

                const data = await response.json();
                console.log("Full JSON Data:", data);

                document.getElementById('sidebar-title').innerText = data.title || "Этап";

                // --- БЕЗОПАСНОЕ ИЗВЛЕЧЕНИЕ УРОКОВ ---
                let lessons = [];
                if (data.module && data.module.lessons) {
                    lessons = data.module.lessons; // Если у тебя структура через module
                } else if (data.lessons) {
                    lessons = data.lessons; // Если маппер отдает их напрямую
                }

                console.log("Extracted Lessons:", lessons);
                this.loadedLessons = lessons;
                this.renderLessons(lessons);

                // Обновляем прогресс-бар, так как могли появиться новые уроки
                if (typeof window.updateProgressBar === 'function') {
                    window.updateProgressBar();
                }

            } catch (err) {
                console.error("Sidebar error:", err);
                // Вместо жесткого падения предлагаем сгенерировать контент
                listContainer.innerHTML = `
                    <div style="text-align: center; padding: 20px;">
                        <p style="color: #666;">Похоже, детали для этого этапа еще не сгенерированы.</p>
                        <button onclick="deepenManager.openModal()"
                                style="background: #4285F4; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
                            Сгенерировать уроки ИИ
                        </button>
                    </div>
                `;
            }
        },

    // Рендеринг списка уроков
    renderLessons(lessons) {
        const container = document.getElementById('lesson-list-container');

        // Проверка на наличие контейнера
        if (!container) {
            console.error("Контейнер lesson-list-container не найден на странице!");
            return;
        }

        // Если уроки пришли как null или пустой массив
        if (!lessons || lessons.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 20px;">
                    <p>Уроков пока нет.</p>
                    <button onclick="deepenManager.openModal()" style="color: #4285F4; border: 1px solid; background: none; padding: 5px 10px; cursor: pointer;">
                        Сгенерировать контент
                    </button>
                </div>`;
            return;
        }

        container.innerHTML = lessons.map(lesson => {
                // Проверяем, есть ли теория (используем флаг из DTO или проверяем объект)
                const isCompleted = lesson.theoryExists || (lesson.theory && lesson.theory.text);
                const iconName = isCompleted ? 'check_circle' : 'auto_stories';
                const cardClass = isCompleted ? 'lesson-card completed' : 'lesson-card';
                const statusText = isCompleted ? 'Теория готова к изучению' : (lesson.description || "Нажми, чтобы открыть теорию");

                return `
                    <div class="${cardClass}" onclick="sidebarManager.loadLessonTheory(${lesson.id})">
                        <div class="lesson-card-icon">
                            <span class="material-icons">${iconName}</span>
                        </div>
                        <div class="lesson-card-info">
                            <h4>${lesson.title || "Без названия"}</h4>
                            <p>${statusText}</p>
                        </div>
                        <div class="lesson-card-status">
                            <span class="material-icons">chevron_right</span>
                        </div>
                    </div>
                `;
            }).join('');
    },

    // 2. Ленивая загрузка теории конкретного урока
    async loadLessonTheory(lessonId) {
        this.currentLessonId = lessonId;
        const theoryContainer = document.getElementById('theory-content');
        this.showDetail();
        theoryContainer.innerHTML = '<div class="loader">ИИ пишет теорию...</div>';

        try {
            const response = await fetch(`/api/v1/roadmap/lesson/${lessonId}`);

            if (!response.ok) throw new Error("Lesson not found");

            const data = await response.json();
            if (data.theory && data.theory.text) {
                const lesson = this.loadedLessons.find(l => l.id === lessonId);
                if (lesson) {
                    lesson.theoryExists = true; // Ставим флаг для рендеринга
                    // Вызываем перекраску узла на карте
                    this.updateNodeColorOnMap(this.currentCheckpointId);
                }

                // --- ДОБАВИТЬ СЮДА ---
                // Теория готова -> урок засчитан -> прогресс вырос!
                if (typeof window.updateProgressBar === 'function') {
                    window.updateProgressBar();
                }
            }

            const markdownContent = data.theory ? data.theory.text : "";

            if (window.marked && markdownContent) {
                    // 1. Рендерим Markdown в HTML
                    theoryContainer.innerHTML = marked.parse(markdownContent);

                    // 2. Ищем все блоки <pre><code> и подсвечиваем их
                    theoryContainer.querySelectorAll('pre code').forEach((block) => {
                        hljs.highlightElement(block);
                    });
                } else {
                    theoryContainer.innerText = markdownContent || "Контент временно недоступен";
                }
            // Рендерим Markdown
        } catch (err) {
            console.error("Ошибка загрузки урока:", err);
            theoryContainer.innerHTML = '<p class="error">Не удалось загрузить теорию</p>';
        }
    },

    // Новый метод для динамического обновления цвета узла
    updateNodeColorOnMap(checkpointId) {
        if (typeof nodes === 'undefined' || !nodes.get(checkpointId)) return;

        const total = this.loadedLessons.length;
        const completed = this.loadedLessons.filter(l => l.theoryExists || (l.theory && l.theory.text)).length;

        let newColor = '#BDC1C6';
        if (completed === total && total > 0) {
            newColor = '#34a853'; // Все зеленые
        } else if (completed > 0) {
            newColor = '#f4af54'; // Часть зеленых
        }

        // Мгновенно обновляем узел в vis.js без перезагрузки всей карты
        nodes.update({
            id: checkpointId,
            color: {
                background: newColor,
                highlight: { background: newColor },
                hover: { background: newColor }
            }
        });
    },

    // Добавь это внутрь объекта sidebarManager
    // Внутри объекта sidebarManager добавь/обнови:

    async sendFeedback() {
        const feedbackInput = document.getElementById('lesson-feedback-input');
        const inputWrapper = feedbackInput.closest('.input-wrapper');
        const statusLabel = document.getElementById('feedback-status');
        const sendBtn = document.getElementById('send-feedback-btn');

        const feedbackText = feedbackInput.value.trim();
        if (!feedbackText) return;

        sendBtn.disabled = true;
        sendBtn.innerText = "Анализируем...";

        try {
            const response = await fetch(`/api/v1/roadmap/${window.roadmapId}/feedback`, {
                method: 'POST',
                headers: { 'Content-Type': 'text/plain' },
                body: feedbackText
            });

            if (response.ok) {
                const updatedConfig = await response.json(); // Получаем RoadmapConfig

                statusLabel.classList.remove('hidden');
                inputWrapper.style.display = 'none'; // Прячем поле ввода
                sendBtn.style.display = 'none'; // Прячем кнопку

                // Показываем обновленные теги
                this.renderUserPreferences(updatedConfig);

                setTimeout(() => {
                    this.closeFeedbackModal();
                    // Сброс интерфейса для следующего раза
                    statusLabel.classList.add('hidden');
                    inputWrapper.style.display = 'block';
                    sendBtn.style.display = 'block';
                    sendBtn.disabled = false;
                    sendBtn.innerText = "Отправить";
                    // Очистка контейнера тегов
                    const prefContainer = document.getElementById('user-prefs-container');
                    if (prefContainer) prefContainer.innerHTML = '';
                }, 7000); // Даем 7 секунд рассмотреть изменения
            }
        } catch (err) {
            console.error("Feedback error:", err);
            alert("Не удалось обновить профиль.");
            sendBtn.disabled = false;
        }
    },

    renderUserPreferences(config) {
        const modalBody = document.querySelector('#feedback-modal .modal-content');
        let prefContainer = document.getElementById('user-prefs-container');

        if (!prefContainer) {
            prefContainer = document.createElement('div');
            prefContainer.id = 'user-prefs-container';
            prefContainer.className = 'user-preferences-display';
            // Вставляем перед кнопками или после заголовка статуса
            modalBody.appendChild(prefContainer);
        }

        const tags = [
            { icon: 'psychology', text: config.mainDomain },
            { icon: 'trending_up', text: config.targetLevel },
            { icon: 'history_edu', text: config.learningStyle },
            { icon: 'record_voice_over', text: config.toneOfVoice }
        ];

        prefContainer.innerHTML = tags
            .filter(t => t.text) // Показываем только заполненные
            .map(t => `
                <div class="pref-tag">
                    <span class="material-icons">${t.icon}</span>
                    ${t.text}
                </div>
            `).join('');
    },

    // Переключение состояний внутри сайдбара
    showList() {
        document.getElementById('lesson-list-container').classList.remove('hidden');
        document.getElementById('lesson-detail-container').classList.add('hidden');
        // Убираем широкий класс, возвращая сайдбар к 450px
        document.getElementById('sidebar').classList.remove('sidebar-expanded');

        if (this.loadedLessons.length > 0) {
                this.renderLessons(this.loadedLessons);
            }
    },

    showDetail() {
        document.getElementById('lesson-list-container').classList.add('hidden');
        document.getElementById('lesson-detail-container').classList.remove('hidden');
        // Добавляем широкий класс (800px) для комфортного чтения
        document.getElementById('sidebar').classList.add('sidebar-expanded');
    }
};