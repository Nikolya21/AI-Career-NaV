/**
 * Логика страницы урока: Markdown и Углубление
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Рендеринг Markdown для всех блоков теории
    if (typeof marked !== 'undefined') {
        document.querySelectorAll('.theory-content').forEach(block => {
            const rawText = block.textContent;
            block.innerHTML = marked.parse(rawText);
        });
    }
});

function requestDeepen(checkpointId) {
    const userPrompt = prompt("Какую подтему вы хотите разобрать подробнее?");
    if (!userPrompt) return;

    // Показываем лоадер (нужно добавить элемент в HTML)
    const loader = document.getElementById('global-loader');
    if (loader) loader.style.display = 'flex';

    fetch(`/api/v1/roadmap/checkpoint/${checkpointId}/deepen`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: userPrompt
    })
    .then(res => res.json())
    .then(data => {
        alert("ИИ создал новый этап! Возвращайтесь на карту, чтобы увидеть его.");
        window.location.href = `/roadmap/${window.roadmapId}`;
    })
    .catch(err => alert("Ошибка при генерации темы"))
    .finally(() => { if (loader) loader.style.display = 'none'; });
}