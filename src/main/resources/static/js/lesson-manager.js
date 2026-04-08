/**
 * Логика страницы урока: Markdown, Поллинг статуса и Углубление
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Извлекаем checkpointId из URL более надежным способом
    const pathParts = window.location.pathname.split('/').filter(p => p !== "");
    const checkpointId = pathParts[pathParts.length - 1];

    // 2. Рендеринг Markdown для всех блоков теории
    if (typeof marked !== 'undefined') {
        document.querySelectorAll('.theory-content').forEach(block => {
            const rawText = block.textContent;
            block.innerHTML = marked.parse(rawText);
        });
    }

    // 3. ЗАПУСК ОПРОСА, если контента еще нет (отображается алерт ожидания)
    const isWaiting = document.querySelector('.alert-info');
    if (isWaiting && checkpointId && !isNaN(checkpointId)) {
        startStatusPolling(checkpointId);
    }
});

/**
 * Функция опроса статуса чекпоинта
 */
function startStatusPolling(checkpointId) {
    console.log("Начинаем опрос статуса для чекпоинта:", checkpointId);

    const pollInterval = setInterval(async () => {
        try {
            const response = await fetch(`/api/v1/roadmap/checkpoint/${checkpointId}/status`);

            if (response.ok) {
                const data = await response.json();
                console.log("Текущий статус:", data.status);

                // Если статус стал ACTIVE — перезагружаем страницу для отображения контента
                if (data.status === 'ACTIVE') {
                    clearInterval(pollInterval);
                    console.log("Контент готов, перезагрузка...");
                    location.reload();
                }
            } else if (response.status === 404) {
                console.error("Чекпоинт не найден, останавливаем опрос");
                clearInterval(pollInterval);
            }
        } catch (err) {
            // При ошибке сети не останавливаемся, просто ждем следующей итерации
            console.warn("Ошибка при опросе статуса (сеть), попробую снова через 5 сек...", err);
        }
    }, 5000);
}

/**
 * Запрос на генерацию дочернего чекпоинта (углубление темы)
 */
function requestDeepen(checkpointId) {
    const userPrompt = prompt("Какую подтему вы хотите разобрать подробнее?");
    if (!userPrompt || userPrompt.trim() === "") return;

    // Показываем глобальный лоадер (если он есть в layout)
    const loader = document.getElementById('global-loader');
    if (loader) loader.style.display = 'flex';

    fetch(`/api/v1/roadmap/checkpoint/${checkpointId}/deepen`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: userPrompt
    })
    .then(res => {
        if (!res.ok) throw new Error("Ошибка сервера");
        return res.json();
    })
    .then(data => {
        alert("ИИ создал новый этап! Возвращайтесь на карту, чтобы увидеть его.");
        // window.roadmapId берется из inline-скрипта в lesson.html
        window.location.href = `/roadmap/${window.roadmapId}`;
    })
    .catch(err => {
        console.error(err);
        alert("Не удалось создать углубленную тему. Попробуйте позже.");
    })
    .finally(() => {
        if (loader) loader.style.display = 'none';
    });
}