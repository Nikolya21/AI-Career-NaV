document.addEventListener('DOMContentLoaded', () => {
  const root = document.getElementById('cabinet-app');
  const grid = document.getElementById('roadmaps-grid');
  const emptyState = document.getElementById('roadmaps-empty');
  const status = document.getElementById('roadmaps-status');

  if (!root || !grid || !emptyState) {
    return;
  }

  const userId = root.dataset.userId;
  if (!userId) {
    if (status) {
      status.textContent = 'Не удалось определить пользователя.';
    }
    emptyState.classList.remove('hidden');
    return;
  }

  loadRoadmaps(userId, grid, emptyState, status);
});

async function loadRoadmaps(userId, grid, emptyState, status) {
  if (status) {
    status.textContent = 'Загружаем ваши roadmap...';
  }

  try {
    const response = await fetch(`/api/v1/roadmap/user/${userId}`);
    if (!response.ok) {
      throw new Error(`Ошибка загрузки списка roadmap: ${response.status}`);
    }

    const roadmaps = await response.json();
    renderRoadmaps(Array.isArray(roadmaps) ? roadmaps : [], grid, emptyState, status);
  } catch (error) {
    console.error('❌ [Cabinet] Не удалось загрузить roadmap пользователя:', error);
    grid.innerHTML = '';
    emptyState.classList.remove('hidden');
    const emptyText = emptyState.querySelector('p');
    if (emptyText) {
      emptyText.textContent = 'Не удалось загрузить список roadmap. Попробуйте обновить страницу.';
    }
    if (status) {
      status.textContent = 'Ошибка загрузки roadmap.';
    }
  }
}

function renderRoadmaps(roadmaps, grid, emptyState, status) {
  grid.innerHTML = '';

  if (!roadmaps.length) {
    emptyState.classList.remove('hidden');
    if (status) {
      status.textContent = 'Пока нет сохранённых roadmap.';
    }
    return;
  }

  emptyState.classList.add('hidden');

  roadmaps.forEach((roadmap) => {
    grid.appendChild(createRoadmapCard(roadmap));
  });

  if (status) {
    status.textContent = `Найдено roadmap: ${roadmaps.length}`;
  }
}

function createRoadmapCard(roadmap) {
  const link = document.createElement('a');
  link.className = `roadmap-card${roadmap.current ? ' current' : ''}`;
  link.href = `/roadmap/${roadmap.id}`;

  const top = document.createElement('div');
  top.className = 'card-top';

  const icon = document.createElement('div');
  icon.className = 'icon-wrapper';
  icon.innerHTML = '<span class="material-icons">auto_awesome</span>';

  const text = document.createElement('div');
  text.className = 'card-text';

  const title = document.createElement('h3');
  title.textContent = roadmap.targetJobTitle || 'Без названия';

  const subtitle = document.createElement('p');
  subtitle.textContent = `Roadmap ID: ${roadmap.id}`;

  text.appendChild(title);
  text.appendChild(subtitle);
  top.appendChild(icon);
  top.appendChild(text);

  const meta = document.createElement('div');
  meta.className = 'card-meta';

  const createdAt = document.createElement('span');
  createdAt.textContent = roadmap.createdAt ? `Создана: ${roadmap.createdAt}` : 'Создана: н/д';

  const badge = document.createElement('span');
  if (roadmap.current) {
    badge.className = 'badge';
    badge.textContent = 'Текущая';
  } else {
    badge.textContent = 'Открыть';
  }

  meta.appendChild(createdAt);
  meta.appendChild(badge);

  const progress = document.createElement('div');
  progress.className = 'card-progress';

  const bar = document.createElement('div');
  bar.className = 'progress-bar';
  const width = Number.isFinite(Number(roadmap.progress)) ? Math.max(0, Math.min(100, Number(roadmap.progress))) : 0;
  bar.style.width = `${width}%`;

  progress.appendChild(bar);

  link.appendChild(top);
  link.appendChild(meta);
  link.appendChild(progress);

  return link;
}

