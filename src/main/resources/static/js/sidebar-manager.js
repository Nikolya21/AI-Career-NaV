const sidebarManager = {
    currentCheckpointId: null,

    // Открыть/закрыть сайдбар
    toggle(show) {
        const sidebar = document.getElementById('sidebar');
        sidebar.classList.toggle('sidebar-hidden', !show);
    },

    // 1. Загрузка чекпоинта (список уроков)
    async loadCheckpoint(checkpointId) {
        this.currentCheckpointId = checkpointId;
        this.toggle(true);
        this.showList(); // Убеждаемся, что видим список, а не старую теорию

        const listContainer = document.getElementById('lesson-list-container');
        listContainer.innerHTML = '<div class="loader">Связываемся с ИИ...</div>';

        try {
            // Тот самый "умный" эндпоинт: вход + генерация скелета уроков
            const response = await fetch(`/api/v1/roadmap/checkpoint/${checkpointId}`);
            const data = await response.json();

            document.getElementById('sidebar-title').innerText = data.title;
            this.renderLessons(data.module.lessons);
        } catch (err) {
            listContainer.innerHTML = '<p class="error">Ошибка загрузки уроков</p>';
        }
    },

    // Рендеринг списка уроков
    renderLessons(lessons) {
        const container = document.getElementById('lesson-list-container');
        if (!lessons || lessons.length === 0) {
            container.innerHTML = "<p>Уроков пока нет. Нажмите 'Углубиться'.</p>";
            return;
        }

        container.innerHTML = lessons.map(lesson => `
            <div class="lesson-card" onclick="sidebarManager.loadLessonTheory(${lesson.id})">
                <span class="material-icons">description</span>
                <div class="lesson-info">
                    <h3>${lesson.title}</h3>
                </div>
            </div>
        `).join('');
    },

    // 2. Ленивая загрузка теории конкретного урока
    async loadLessonTheory(lessonId) {
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

    // Переключение состояний внутри сайдбара
    showList() {
        document.getElementById('lesson-list-container').classList.remove('hidden');
        document.getElementById('lesson-detail-container').classList.add('hidden');
    },

    showDetail() {
        document.getElementById('lesson-list-container').classList.add('hidden');
        document.getElementById('lesson-detail-container').classList.remove('hidden');
    }
};