let network = null;
let nodes = new vis.DataSet([]);
let edges = new vis.DataSet([]);

document.addEventListener('DOMContentLoaded', () => {
    loadRoadmapFromServer();
});

async function loadRoadmapFromServer() {
    const loader = document.getElementById('global-loader');
    const roadmapId = window.roadmapId;

    if (!roadmapId) {
        console.error("ID дорожной карты не найден в window.roadmapId");
        return;
    }

    loader.classList.remove('hidden');

    try {
        const response = await fetch(`/api/v1/roadmap/${roadmapId}/graph-data`);

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const data = await response.json();
        initGraph(data);

    } catch (error) {
        console.error('Ошибка при загрузке Roadmap:', error);
        alert('Не удалось загрузить данные дорожной карты. Проверьте консоль.');
    } finally {
        loader.classList.add('hidden');
    }
}

function initGraph(data) {
    nodes.clear();
    edges.clear();

    // 1. Центральный узел (Профессия) всегда рисуем как 0,
    // но делаем его визуально уникальным (чтобы отличался от других)
    nodes.add({
        id: 0,
        label: window.jobTitle || "Roadmap",
        color: '#FFD700', // Золотой корень
        shape: 'dot',    // Форма звезды
        size: 35
    });

    if (data.topics && Array.isArray(data.topics)) {
        data.topics.forEach(topic => {
            const topicNodeId = 'topic_' + topic.id;

            nodes.add({
                id: topicNodeId,
                label: topic.topicTitle,
                color: '#ffffff',
                border: '#FBBC05',
                shape: 'dot',
                size: 22,
                borderWidth: 2
            });

            // Связываем тему с центром (id: 0)
            edges.add({ from: 0, to: topicNodeId, color: '#DADCE0' });

            if (topic.checkpoints && Array.isArray(topic.checkpoints)) {
                topic.checkpoints.forEach(cp => {
                    renderCheckpointNode(cp, topicNodeId);
                });
            }
        });
    }

    const container = document.getElementById('roadmap-container');
    const options = {
        physics: {
            enabled: true,
            stabilization: { iterations: 1000 },
            barnesHut: { gravitationalConstant: -3000, springLength: 150 }
        },
        interaction: { hover: true }
    };
    network = new vis.Network(container, { nodes, edges }, options);
    setupEventListeners();
}

function renderCheckpointNode(cp, topicNodeId) {
    // Если бэкенд случайно прислал ROOT внутри темы (например, если ты пофиксишь topicId),
    // мы его игнорируем, так как уже нарисовали центральную звезду.
    if (cp.type === 'ROOT') {
        window.realRootId = cp.id; // Запоминаем реальный ID рута из БД!
        return;
    }

    let nodeColor = '#BDC1C6';
    if (cp.status === 'COMPLETED') nodeColor = '#34A853';
    if (cp.status === 'ACTIVE') nodeColor = '#4285F4';

    if (!nodes.get(cp.id)) {
        nodes.add({
            id: cp.id,
            label: cp.title || "Без названия",
            color: nodeColor,
            shape: 'dot',
            size: 16
        });
    }

    let fromId = topicNodeId;

    // --- УМНАЯ СВЯЗЬ ---
    if (cp.parentCheckpointId) {
        // Если родитель - это наш реальный ROOT из базы (176), цепляем напрямую к центру (0)
        if (cp.parentCheckpointId === window.realRootId) {
            fromId = 0;
        }
        // Если родитель есть в графе (это углубление внутри MAIN)
        else if (nodes.get(cp.parentCheckpointId)) {
            fromId = cp.parentCheckpointId;
        }
        // Иначе оставляем привязку к topicNodeId
    }

    edges.add({
        from: fromId,
        to: cp.id,
        dashes: (fromId !== topicNodeId && fromId !== 0),
        color: '#DADCE0'
    });
}


function setupEventListeners() {
    network.on("hoverNode", (params) => {
        if (Number.isInteger(params.node) && params.node !== 0) {
            showNodeTooltip(params.node);
        }
    });

    network.on("blurNode", () => hideNodeTooltip());

    network.on("click", (params) => {
        if (params.nodes.length > 0) {
            const selectedId = params.nodes[0];

            // Если кликнули по центральной звезде
            if (selectedId === 0) {
                alert("Это корень вашей профессии! Выберите конкретный этап (узел) для начала обучения.");
                return;
            }

            if (Number.isInteger(selectedId)) {
                if (typeof sidebarManager !== 'undefined') {
                    sidebarManager.loadCheckpoint(selectedId);
                }
            }
        }
    });
}

function showNodeTooltip(nodeId) {
    const pos = network.canvasToDOM(network.getPositions([nodeId])[nodeId]);
    const tooltip = document.getElementById('node-tooltip');
    if (!tooltip) return;

    tooltip.classList.remove('hidden');
    tooltip.style.left = pos.x + 'px';
    tooltip.style.top = (pos.y - 40) + 'px';
    tooltip.dataset.targetNodeId = nodeId;
}

function hideNodeTooltip() {
    setTimeout(() => {
        const tooltip = document.getElementById('node-tooltip');
        if (tooltip) tooltip.classList.add('hidden');
    }, 300);
}