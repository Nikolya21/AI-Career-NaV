/**
 * Логика визуализации дорожной карты в стиле Google / Lexitutor
 */
document.addEventListener('DOMContentLoaded', function () {
    const data = window.roadmapData;
    if (!data) return;

    const nodes = new vis.DataSet([]);
    const edges = new vis.DataSet([]);

    // 1. Центральный узел — Цель
    nodes.add({
        id: 0,
        label: data.jobTitle,
        color: { background: '#4285F4', border: '#1A73E8' },
        size: 35,
        shape: 'dot',
        font: { color: '#ffffff', size: 16 }
    });

    // 2. Обработка данных из БД
    data.topics.forEach((topic) => {
        const topicNodeId = 't' + topic.id;

        // Узел Темы (Блок)
        nodes.add({
            id: topicNodeId,
            label: topic.topicTitle,
            color: { background: '#ffffff', border: '#FBBC05' },
            size: 25,
            shape: 'dot',
            borderWidth: 2,
            font: { size: 14, weight: '500' }
        });
        edges.add({ from: 0, to: topicNodeId, color: '#E8EAED' });

        // Узлы Чекпоинтов
        topic.checkpoints.forEach((cp) => {
            let nodeColor = '#BDC1C6'; // LOCKED
            if (cp.status === 'COMPLETED') nodeColor = '#34A853';
            if (cp.status === 'ACTIVE') nodeColor = '#4285F4';

            nodes.add({
                id: cp.id,
                label: cp.title,
                color: { background: nodeColor, border: nodeColor },
                size: 18,
                shape: 'dot',
                font: { size: 12 },
                chosen: { node: (values) => { values.shadow = true; } }
            });

            // Связь с темой или родителем
            const fromId = cp.parentCheckpointId ? cp.parentCheckpointId : topicNodeId;
            edges.add({
                from: fromId,
                to: cp.id,
                dashes: !!cp.parentCheckpointId,
                color: '#DADCE0'
            });
        });
    });

    const container = document.getElementById('roadmap-container');
    const network = new vis.Network(container, { nodes, edges }, {
        interaction: { hover: true, tooltipDelay: 200 },
        physics: {
            enabled: true,
            barnesHut: { gravitationalConstant: -3000, centralGravity: 0.3, springLength: 95 }
        }
    });

    // Появление кнопки "Углубиться" при наведении
    network.on("hoverNode", function (params) {
        if (typeof params.node === 'number') { // Если это чекпоинт
            const nodePos = network.canvasToDOM(network.getPositions([params.node])[params.node]);
            const tooltip = document.getElementById('node-tooltip');
            tooltip.style.display = 'block';
            tooltip.style.left = nodePos.x + 'px';
            tooltip.style.top = (nodePos.y - 50) + 'px';
            tooltip.dataset.nodeId = params.node;
        }
    });

    network.on("blurNode", () => {
        // Скрываем с небольшой задержкой, чтобы можно было успеть кликнуть
        setTimeout(() => { document.getElementById('node-tooltip').style.display = 'none'; }, 2000);
    });

    // Переход в урок
    network.on("click", function (params) {
        if (params.nodes.length > 0 && typeof params.nodes[0] === 'number') {
            window.location.href = `/roadmap/lesson/${params.nodes[0]}`;
        }
    });
});

// Функция отправки фидбека (глобальная)
function sendGlobalFeedback() {
    const feedbackInput = document.getElementById('feedback-text');
    const feedbackText = feedbackInput.value.trim();

    if (!feedbackText) {
        alert("Пожалуйста, напишите что-нибудь, чтобы ИИ мог подстроиться.");
        return;
    }

    // Блокируем кнопку на время запроса
    const btn = event.target;
    btn.disabled = true;
    btn.innerText = "Обновляю профиль...";

    fetch(`/api/v1/roadmap/${window.roadmapData.id}/feedback`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: feedbackText
    })
    .then(response => {
        if (response.ok) {
            alert("Ваши предпочтения учтены! Следующие уроки будут адаптированы.");
            feedbackInput.value = ""; // Очищаем поле
        } else {
            alert("Произошла ошибка при отправке фидбека.");
        }
    })
    .catch(err => {
        console.error('Feedback error:', err);
        alert("Не удалось связаться с сервером.");
    })
    .finally(() => {
        btn.disabled = false;
        btn.innerText = "Обновить мой стиль";
    });
}