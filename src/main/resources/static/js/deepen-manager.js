document.addEventListener('DOMContentLoaded', () => {
    const triggerBtn = document.getElementById('deepen-trigger-btn');
    const confirmBtn = document.getElementById('confirm-deepen-btn');

    // Проверяем, существует ли triggerBtn перед тем, как вешать событие
    if (triggerBtn) {
        triggerBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const tooltip = document.getElementById('node-tooltip');
            if (tooltip) {
                window.currentDeepenNodeId = tooltip.dataset.targetNodeId;
                document.getElementById('deepen-modal').classList.remove('hidden');
            }
        });
    }

    // Проверяем confirmBtn (он у нас есть в модалке)
    /*[cite: 5] */
    // Внутри confirmBtn.addEventListener('click', ...)
    if (confirmBtn) {
        confirmBtn.addEventListener('click', async () => {
            const text = document.getElementById('deepen-input').value;
            if (!text) return;

            // 1. Мгновенно закрываем модалку, не дожидаясь ответа сервера
            closeDeepenModal();

            // 2. Вместо лоадера на весь экран можно добавить легкую индикацию
            // на самой родительской вершине (опционально)

            try {
                const response = await fetch(`/api/v1/roadmap/lesson/${window.currentDeepenLessonId}/deepen`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'text/plain' },
                    body: text
                });

                if (response.ok) {
                    const newCp = await response.json();

                    // 3. Вызываем функцию "магического" добавления узла
                    createNewNodeAnimated(newCp);
                }
            } catch (err) {
                console.error("Ошибка связи с ИИ:", err);
                // Вместо алерта лучше использовать неброское уведомление (toast)
            }
        });
    }
});

function startLongAcceleratingPulse() {
    const bgWrapper = document.querySelector('.bg-wrapper');
    if (!bgWrapper) return;

    bgWrapper.classList.add('bg-active-pulse');

    let currentDuration = 1.0; // Начинаем с очень спокойного ритма (1 сек)
    const minDuration = 0.15;  // Пик частоты (0.15 сек)
    const totalTime = 17000;   // Целевое время 17 сек
    const stepTime = 500;      // Обновляем скорость каждые полсекунды
    const steps = totalTime / stepTime;
    const durationDecrement = (currentDuration - minDuration) / steps;

    bgWrapper.style.setProperty('--pulse-duration', `${currentDuration}s`);

    const accelInterval = setInterval(() => {
        if (currentDuration > minDuration) {
            currentDuration -= durationDecrement;
            bgWrapper.style.setProperty('--pulse-duration', `${currentDuration}s`);
        } else {
            // Если сервер еще не ответил, а 17 сек прошло — остаемся на пике
            bgWrapper.style.setProperty('--pulse-duration', `${minDuration}s`);
        }
    }, stepTime);

    bgWrapper.dataset.accelInterval = accelInterval;
}

// Вызывай это СРАЗУ при клике на кнопку отправки в модалке
document.getElementById('confirm-deepen-btn').onclick = function() {
    startLongAcceleratingPulse();
    // Дальше твой Fetch/Axios запрос на сервер...
};

const deepenManager = {
    // Этот метод вызывается кнопкой "Углубиться" из сайдбара урока
    openModal() {
        // sidebarManager сохраняет ID чекпоинта, когда вы открываете его список уроков или теорию
        const nodeId = sidebarManager.currentCheckpointId;
        const lessonId = sidebarManager.currentLessonId;

        if (!nodeId) {
            alert("Ошибка: не выбран этап для углубления");
            return;
        }

        if (!lessonId) {
             alert("Ошибка: откройте урок, чтобы углубиться в тему");
             return;
        }

        // Сохраняем ID в глобальную переменную, которую использует твой confirmBtn.addEventListener
        window.currentDeepenNodeId = nodeId;

        window.currentDeepenLessonId = lessonId;

        // Показываем модалку
        document.getElementById('deepen-modal').classList.remove('hidden');
    }
};

function closeDeepenModal() {
    document.getElementById('deepen-modal').classList.add('hidden');
    document.getElementById('deepen-input').value = '';
}

function showGlobalLoader(show) {
    document.getElementById('global-loader').classList.toggle('hidden', !show);
}