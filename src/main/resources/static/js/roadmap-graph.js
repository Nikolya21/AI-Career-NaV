let network = null;
let nodes = new vis.DataSet([]);
let edges = new vis.DataSet([]);

// 1. В начале файла добавим объект для хранения интервалов анимации
const typingIntervals = {};

// 2. Функция для "печатания" текста
function typeText(nodeId, fullText) {
    let currentText = nodes.get(nodeId).label.replace('...', '');
    let i = currentText.length;

    clearInterval(typingIntervals[nodeId]);

    typingIntervals[nodeId] = setInterval(() => {
        if (i < fullText.length) {
            currentText += fullText[i];
            nodes.update({ id: nodeId, label: currentText });
            i++;
        } else {
            clearInterval(typingIntervals[nodeId]);
        }
    }, 30); // Скорость печати (30мс на символ)
}

// 3. Функция для мгновенного отката к 20 символам
function resetText(nodeId, fullText) {
    clearInterval(typingIntervals[nodeId]);
    nodes.update({
        id: nodeId,
        label: truncateLabel(fullText, 20)
    });
}

// Хранилище для активных импульсов
let activeImpulses = [];

class Impulse {
    constructor(fromNode, toNode, edge, color) {
        this.from = network.getPosition(fromNode);
        this.to = network.getPosition(toNode);
        this.edge = edge;
        this.progress = 0;
        this.speed = 0.015; // Скорость волны
        this.color = color; // Цвет импульса
    }

    update() {
        // Нелинейное приращение: чем ближе к концу, тем меньше шаг
        let remaining = 1 - this.progress;
        this.progress += this.speed * (remaining + 0.2);
        return this.progress < 0.99;
    }

    draw(ctx) {
        // Вычисляем текущую позицию точки на ребре
        const x = this.from.x + (this.to.x - this.from.x) * this.progress;
        const y = this.from.y + (this.to.y - this.from.y) * this.progress;

        ctx.beginPath();
        ctx.arc(x, y, 4, 0, Math.PI * 2); // Размер "нейрона"
        ctx.fillStyle = this.color;

        // Добавляем свечение (glow effect)
        ctx.shadowBlur = 12;
        ctx.shadowColor = this.color;

        ctx.fill();
        ctx.closePath();

        // Сбрасываем тень, чтобы не влиять на остальной граф
        ctx.shadowBlur = 0;
    }
}

// Запуск цикла анимации
function animate() {
    if (activeImpulses.length > 0) {
        activeImpulses = activeImpulses.filter(imp => imp.update());
        network.redraw(); // Перерисовываем канвас
        requestAnimationFrame(animate);
    }
}

function setupEventListeners() {
    // --- НАВЕДЕНИЕ (Теперь здесь срабатывает импульс) ---
    network.on("hoverNode", (params) => {
        const nodeId = params.node;
        const nodeData = nodes.get(nodeId);

        // 1. Запуск импульса по инцидентным ребрам
        if (nodeId !== 0) {
            handleNodeImpulse(nodeId);
        }

        // 2. Логика печати текста
        if (nodeId !== 0 && nodeData && nodeData.fullTitle && nodeData.label.includes('...')) {
            typeText(nodeId, nodeData.fullTitle);
        }

        // 3. Показ тултипа
        if (nodeId !== 0) {
            showNodeTooltip(nodeId);
        }
    });

    network.on("blurNode", (params) => {
        const nodeId = params.node;
        const nodeData = nodes.get(nodeId);
        if (nodeId !== 0 && nodeData && nodeData.fullTitle) {
            resetText(nodeId, nodeData.fullTitle);
        }
        hideNodeTooltip();
    });

    // --- КЛИК (Тень и увеличение ребра) ---
    network.on("click", async (params) => {
        if (params.nodes.length === 0) {
            // Если кликнули в пустоту — сбрасываем выделение ребер
            resetEdgesStyle();
            return;
        }

        const selectedId = params.nodes[0];

        // 1. Логика для центра (ROOT)
        if (selectedId === 0) {
            handleRootClick();
            return;
        }

        // 2. Логика для обычных узлов
        if (Number.isInteger(selectedId)) {
            // Визуально выделяем инцидентные ребра
            highlightConnectedEdges(selectedId);

            // Загружаем контент в сайдбар[cite: 4]
            if (typeof sidebarManager !== 'undefined') {
                await sidebarManager.loadCheckpoint(selectedId);

                // СРАЗУ ПОСЛЕ ЗАГРУЗКИ:
                // Если это был первый заход и уроки создались,
                // прогресс изменится (так как totalLessons вырос)
            }
        }
    });

    network.on("afterDrawing", (ctx) => {
        activeImpulses.forEach(imp => imp.draw(ctx));
    });
}

async function handleRootClick() {
    try {
        const response = await fetch(`/api/v1/roadmap/${window.roadmapId}/root-action`);

        if (response.ok) {
            const data = await response.json();
            // Выполняем переход на URL, который прислал бэкенд
            window.location.href = data.redirectUrl;
        } else {
            console.error(`Ошибка сервера: ${response.status}`);
        }
    } catch (error) {
        console.error("Ошибка при получении ссылки редиректа:", error);
    }
}

// Подсветка ребер: увеличение толщины и наложение тени
/* */
function highlightConnectedEdges(nodeId) {
    resetEdgesStyle();

    const nodeData = nodes.get(nodeId);
    const nodeColor = (typeof nodeData.color === 'object') ? nodeData.color.background : nodeData.color;

    const connectedEdges = network.getConnectedEdges(nodeId);

    connectedEdges.forEach(edgeId => {
        edges.update({
            id: edgeId,
            width: 4,
            // Красим ребро строго в цвет узла, без синих примесей
            color: { color: nodeColor, highlight: nodeColor, hover: nodeColor },
            shadow: {
                enabled: true,
                color: nodeColor,
                size: 10
            }
        });

        const edge = edges.get(edgeId);
        const targetNodeId = (edge.from === nodeId) ? edge.to : edge.from;
        activeImpulses.push(new Impulse(nodeId, targetNodeId, edge, nodeColor));
    });

    if (activeImpulses.length > 0) animate();
}

function handleNodeImpulse(nodeId) {
    const nodeData = nodes.get(nodeId);
    const nodeColor = (typeof nodeData.color === 'object') ? nodeData.color.background : nodeData.color;
    const connectedEdges = network.getConnectedEdges(nodeId);

    connectedEdges.forEach(edgeId => {
        const edge = edges.get(edgeId);
        const targetNodeId = (edge.from === nodeId) ? edge.to : edge.from;

        // ДОБАВЬ edge аргументом:
        activeImpulses.push(new Impulse(nodeId, targetNodeId, edge, nodeColor));
    });

    if (activeImpulses.length > 0) animate();
}

// Возврат ребер в исходное состояние
function resetEdgesStyle() {
    edges.forEach(edge => {
        edges.update({
            id: edge.id,
            width: 2,
            shadow: { enabled: false },
            color: { color: '#9AA0A6' }
        });
    });
}


document.addEventListener('DOMContentLoaded', () => {
    loadRoadmapFromServer();
    updateProgressBar(); // Первый запуск при загрузке страниц
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

function truncateLabel(text, limit = 20) {
    if (!text) return "";
    return text.length > limit ? text.substring(0, limit) + "..." : text;
}

function initGraph(data) {
    nodes.clear();
    edges.clear();

    // 1. Центральный узел (Профессия) всегда рисуем как 0,
    // но делаем его визуально уникальным (чтобы отличался от других)
    nodes.add({
        id: 0,
        label: window.jobTitle || "Roadmap",
        shape: 'dot',
        size: 35,
        font: { color: '#202124', size: 16, face: 'Google Sans', weight: '500' },
        color: {
            background: '#FFFFFF',
            border: '#4285F4', // Яркий синий ободок
            highlight: { background: '#F1F3F4', border: '#1A73E8' }
        },
        borderWidth: 3,
        shadow: { enabled: true, color: 'rgba(0,0,0,0.1)', size: 10, x: 0, y: 4 }
    });

    if (data.topics && Array.isArray(data.topics)) {
        data.topics.forEach(topic => {
            const topicNodeId = 'topic_' + topic.id;

            nodes.add({
                id: topicNodeId,
                label: truncateLabel(topic.topicTitle),
                color: '#ffffff',
                shape: 'dot',
                size: 20,
                font: { color: '#3C4043', size: 13, face: 'Google Sans' },
                color: {
                    background: '#FFFFFF',
                    border: '#DADCE0', // Серый ободок в покое
                    highlight: { background: '#E8F0FE', border: '#4285F4' }
                },
                borderWidth: 2
            });

            // Связываем тему с центром (id: 0)
            edges.add({
                from: 0,
                to: topicNodeId,
                color: { color: '#9AA0A6' } // Средне-серый для основных магистралей[cite: 7]
            });

            if (topic.checkpoints && Array.isArray(topic.checkpoints)) {
                topic.checkpoints.forEach(cp => {
                    renderCheckpointNode(cp, topicNodeId);
                });
            }
        });
    }

    const container = document.getElementById('roadmap-container');
    const options = {
        nodes: {
            font: { face: 'Google Sans' },
            borderWidth: 2,
            // Выключаем стандартное синее выделение
            chosen: {
                node: function(values, id, selected, hovering) {
                    // Мы не меняем цвет рамки на синий, оставляем как есть
                }
            }
        },
        physics: {
            enabled: true,
            stabilization: { iterations: 1000 },
            barnesHut: { gravitationalConstant: -3000, springLength: 150 }
        },
        edges: {
            width: 2,
            color: {
                color: '#80868B', // Более темный серый (вместо бледного #DADCE0)
            },
            arrows: {
                to: { enabled: false }
            },

            shadow: {
                enabled: true,
                color: 'rgba(0,0,0,0.05)',
                size: 3,
                x: 1,
                y: 1
            },
            // Возвращаем стандартную плавность линий, которая была в vis-network изначально
            smooth: {
                enabled: true,
                roundness: 0.5
            }
        },
        interaction: {
            hover: true,
            tooltipDelay: 200,
            hoverConnectedEdges: true, // Подсвечивать ребра при наведении на узел[cite: 7]
            selectConnectedEdges: true
        }
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

    // Базовый цвет (серый для неначатых)
    let nodeColor = '#BDC1C6';

    // Логика для MAIN чекпоинтов
    if (cp.type === 'MAIN') {
        const total = cp.totalLessons || 0;
        const completed = cp.completedLessons || 0;

        if (completed === total && total > 0) {
            nodeColor = '#34a853'; // Зеленый (все уроки пройдены)
        } else if (completed > 0) {
            nodeColor = '#f4af54'; // Оранжевый (есть начатые уроки)
        }
    } else {
        // Логика для остальных типов (например, DEEPEN)
        if (cp.status === 'COMPLETED') nodeColor = '#34A853';
        if (cp.status === 'ACTIVE') nodeColor = '#4285F4';
    }

    if (!nodes.get(cp.id)) {
        nodes.add({
            id: cp.id,
            label: truncateLabel(cp.title || "Без названия"),
            fullTitle: cp.title || "Без названия",
            title: "",
            // ЗАМЕНЯЕМ color: nodeColor НА ОБЪЕКТ НИЖЕ:
            color: {
                background: nodeColor,
                border: '#ffffff',
                highlight: {
                    background: nodeColor, // Цвет при клике (остается прежним)
                    border: '#4285F4'      // Синяя рамка для акцента
                },
                hover: {
                    background: nodeColor, // Цвет при наведении (остается прежним)
                    border: '#ffffff'
                }
            },
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
        color: {
                color: (fromId !== topicNodeId && fromId !== 0) ? '#BDC1C6' : '#9AA0A6'
            },
        width: 1.5 // Второстепенные связи можно оставить чуть тоньше
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

window.updateProgressBar = async function() {
    const roadmapId = window.roadmapId;
    if (!roadmapId) return;

    try {
        const response = await fetch(`/api/v1/roadmap/${roadmapId}/progress`);
        if (response.ok) {
            const progress = await response.json(); // Получаем те самые 29.3

            const fill = document.getElementById('progress-fill');
            const text = document.getElementById('progress-percentage');

            if (fill) fill.style.width = progress + '%';
            if (text) text.innerText = Math.round(progress) + '%';
        }
    } catch (e) {
        console.error("Ошибка обновления прогресс-бара", e);
    }
};