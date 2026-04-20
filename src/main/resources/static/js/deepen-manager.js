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
    if (confirmBtn) {
        confirmBtn.addEventListener('click', async () => {
            const text = document.getElementById('deepen-input').value;
            if (!text) return;

            closeDeepenModal();
            showGlobalLoader(true);

            try {
                const response = await fetch(`/api/v1/roadmap/checkpoint/${window.currentDeepenNodeId}/deepen`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'text/plain' },
                    body: text
                });

                if (response.ok) {
                    const newCp = await response.json();

                    // Динамическое добавление в граф (без перезагрузки!)
                    nodes.add({
                        id: newCp.id,
                        label: newCp.title,
                        color: '#4285F4', // Новый всегда ACTIVE
                        shape: 'dot',
                        size: 16
                    });

                    edges.add({
                        from: newCp.parentCheckpointId,
                        to: newCp.id,
                        dashes: true,
                        color: '#4285F4'
                    });

                    // Включаем физику, чтобы узел нашел место
                    network.setOptions({ physics: { enabled: true } });
                    setTimeout(() => network.setOptions({ physics: { enabled: false } }), 2000);
                }
            } catch (err) {
                alert("Ошибка связи с ИИ");
            } finally {
                showGlobalLoader(false);
            }
        });
    }
});

const deepenManager = {
    // Этот метод вызывается кнопкой "Углубиться" из сайдбара урока
    openModal() {
        // sidebarManager сохраняет ID чекпоинта, когда вы открываете его список уроков или теорию
        const nodeId = sidebarManager.currentCheckpointId;

        if (!nodeId) {
            alert("Ошибка: не выбран этап для углубления");
            return;
        }

        // Сохраняем ID в глобальную переменную, которую использует твой confirmBtn.addEventListener
        window.currentDeepenNodeId = nodeId;

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