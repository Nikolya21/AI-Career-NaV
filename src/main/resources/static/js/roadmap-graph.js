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

    // 1. Центральный узел (Профессия)
    // ID 0 зарезервирован для центра
    nodes.add({
        id: 0,
        label: window.jobTitle || "Roadmap",
        color: '#4285F4',
        shape: 'dot',
        size: 30
    });

    if (data.topics && Array.isArray(data.topics)) {
        data.topics.forEach(topic => {
            const topicNodeId = 'topic_' + topic.id;

            // Добавляем узел темы
            nodes.add({
                id: topicNodeId,
                label: topic.topicTitle,
                color: '#ffffff',
                border: '#FBBC05',
                shape: 'dot',
                size: 22,
                borderWidth: 2
            });

            // Связываем тему с центром
            edges.add({ from: 0, to: topicNodeId, color: '#DADCE0' });

            // Отрисовываем чекпоинты этой темы
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
    let nodeColor = '#BDC1C6';
    if (cp.status === 'COMPLETED') nodeColor = '#34A853';
    if (cp.status === 'ACTIVE') nodeColor = '#4285F4';

    // 1. Добавляем узел (если его еще нет)
    if (!nodes.get(cp.id)) {
        nodes.add({
            id: cp.id,
            label: cp.title || "Без названия", // Защита от null
            color: nodeColor,
            shape: 'dot',
            size: 16
        });
    }

    // 2. Логика связи
    let fromId = topicNodeId; // По умолчанию цепляем к теме

    // Проверяем: есть ли родитель и существует ли он уже в графе?
    if (cp.parentCheckpointId && nodes.get(cp.parentCheckpointId)) {
        fromId = cp.parentCheckpointId;
    }

    edges.add({
        from: fromId,
        to: cp.id,
        dashes: (fromId !== topicNodeId), // Пунктир, если это связь между чекпоинтами
        color: '#DADCE0'
    });
}

function setupEventListeners() {
    network.on("hoverNode", (params) => {
        // Показываем тултип только для чекпоинтов (числовые ID), а не для тем (строковые ID)
        if (Number.isInteger(params.node) && params.node !== 0) {
            showNodeTooltip(params.node);
        }
    });

    network.on("blurNode", () => hideNodeTooltip());

    network.on("click", (params) => {
        if (params.nodes.length > 0) {
            const selectedId = params.nodes[0];
            // Если кликнули по чекпоинту — открываем сайдбар
            if (Number.isInteger(selectedId) && selectedId !== 0) {
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