const sidebarManager = {
    currentCheckpointId: null,
    currentLessonId: null, // Добавляем новое поле

    // Открыть/закрыть сайдбар
    toggle(show) {
        const sidebar = document.getElementById('sidebar');
        sidebar.classList.toggle('sidebar-hidden', !show);
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
                this.renderLessons(lessons);

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

        // Отрисовка
        container.innerHTML = lessons.map(lesson => `
            <div class="lesson-card" onclick="sidebarManager.loadLessonTheory(${lesson.id})">
                <span class="material-icons">description</span>
                <div class="lesson-info">
                    <h3>${lesson.title || "Без названия"}</h3>
                </div>
            </div>
        `).join('');
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

            const markdownContent = data.theory ? data.theory.text : "";

            if (window.marked && markdownContent) {
                theoryContainer.innerHTML = marked.parse(markdownContent); // Исправлено на theoryContainer
            } else {
                theoryContainer.innerText = markdownContent || "Контент временно недоступен";
            }
            // Рендерим Markdown
        } catch (err) {
            console.error("Ошибка загрузки урока:", err);
            theoryContainer.innerHTML = '<p class="error">Не удалось загрузить теорию</p>';
        }
    },

    // Добавь это внутрь объекта sidebarManager
    async sendFeedback() {
        const feedbackInput = document.getElementById('lesson-feedback-input');
        const statusLabel = document.getElementById('feedback-status');
        const sendBtn = document.getElementById('send-feedback-btn');

        const feedbackText = feedbackInput.value.trim();
        if (!feedbackText) return;

        // roadmapId мы берем из глобальной переменной window.roadmapId
        const roadmapId = window.roadmapId;

        sendBtn.disabled = true;
        sendBtn.innerText = "Отправка...";

        try {
            const response = await fetch(`/api/v1/roadmap/${roadmapId}/feedback`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/plain' // Твой контроллер принимает @RequestBody String
                },
                body: feedbackText
            });

            if (response.ok) {
                feedbackInput.value = '';
                statusLabel.classList.remove('hidden');
                sendBtn.classList.add('hidden');

                // Скрываем сообщение через 5 секунд и возвращаем кнопку
                setTimeout(() => {
                    statusLabel.classList.add('hidden');
                    sendBtn.classList.remove('hidden');
                    sendBtn.disabled = false;
                    sendBtn.innerHTML = '<span class="material-icons">send</span> Отправить фидбек';
                }, 5000);
            } else {
                throw new Error("Ошибка сервера");
            }
        } catch (err) {
            console.error("Feedback error:", err);
            alert("Не удалось отправить отзыв. Попробуй позже.");
            sendBtn.disabled = false;
            sendBtn.innerHTML = '<span class="material-icons">send</span> Отправить фидбек';
        }
    },

    // Переключение состояний внутри сайдбара
    showList() {
        document.getElementById('lesson-list-container').classList.remove('hidden');
        document.getElementById('lesson-detail-container').classList.add('hidden');
        // Убираем широкий класс, возвращая сайдбар к 450px
        document.getElementById('sidebar').classList.remove('sidebar-expanded');
    },

    showDetail() {
        document.getElementById('lesson-list-container').classList.add('hidden');
        document.getElementById('lesson-detail-container').classList.remove('hidden');
        // Добавляем широкий класс (800px) для комфортного чтения
        document.getElementById('sidebar').classList.add('sidebar-expanded');
    }
};