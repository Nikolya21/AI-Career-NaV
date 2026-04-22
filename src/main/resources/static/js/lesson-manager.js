/**
 * Логика страницы урока: Markdown, Поллинг статуса, Углубление и Интерактивные задания
 */
document.addEventListener('DOMContentLoaded', function () {
  // 1. Извлекаем checkpointId из URL
  const pathParts = window.location.pathname.split('/').filter(p => p !== "");
  const checkpointId = pathParts[pathParts.length - 1];

  // 2. Рендеринг Markdown для всех блоков теории
  if (typeof marked !== 'undefined') {
    document.querySelectorAll('.theory-content').forEach(block => {
      const rawText = block.textContent;
      block.innerHTML = marked.parse(rawText);
    });
  }

  // 3. Рендеринг интерактивных виджетов задач
  renderTaskWidgets();

  // 4. Запуск опроса статуса, если контент ещё не готов
  const isWaiting = document.querySelector('.alert-info');
  if (isWaiting && checkpointId && !isNaN(checkpointId)) {
    startStatusPolling(checkpointId);
  }
});

// ============================================================
// РЕНДЕРИНГ ВИДЖЕТОВ ЗАДАЧ
// ============================================================

/**
 * Для каждого .task-widget считывает data-атрибуты и отрисовывает
 * соответствующий интерактивный компонент.
 */
function renderTaskWidgets() {
  document.querySelectorAll('.task-widget').forEach(widget => {
    const taskId    = widget.dataset.taskId;
    const taskType  = widget.dataset.taskType;
    const taskTitle = widget.dataset.taskTitle || '';
    let content;
    try {
      const raw = widget.dataset.taskContent;
      content = raw ? JSON.parse(raw) : {};
    } catch (e) {
      widget.innerHTML = '<p style="color:#C5221F">Ошибка загрузки задачи</p>';
      return;
    }
    widget.innerHTML = buildTaskHTML(taskId, taskType, taskTitle, content);
  });
}

function buildTaskHTML(taskId, type, title, content) {
  let body = '';
  switch (type) {
    case 'SINGLE_CHOICE': case 'QUIZ':
      body = buildSingleChoiceHTML(taskId, content); break;
    case 'TRUE_FALSE':
      body = buildTrueFalseHTML(taskId, content); break;
    case 'MATCHING':
      body = buildMatchingHTML(taskId, content); break;
    case 'FILL_BLANK':
      body = buildFillBlankHTML(taskId, content); break;
    case 'ORDERING':
      body = buildOrderingHTML(taskId, content); break;
    case 'OPEN_QUESTION':
      body = buildOpenQuestionHTML(taskId, content); break;
    case 'PRACTICE': case 'CODE_SNIPPET':
      body = buildPracticeHTML(taskId, content, type); break;
    default:
      body = `<p style="color:#5F6368">Тип задачи: ${escHtml(type)}</p>`;
  }
  return `
      <div class="task-header">
        <span class="task-type-badge ${badgeClass(type)}">${badgeLabel(type)}</span>
        <strong>${escHtml(title)}</strong>
      </div>
      ${body}
      <div id="feedback-${taskId}" class="task-feedback"></div>`;
}

// ---- Тип: SINGLE_CHOICE / QUIZ ----
function buildSingleChoiceHTML(taskId, c) {
  const q = escHtml(c.question || '');
  const opts = (c.options || []).map((opt, i) =>
      `<li data-index="${i}" onclick="selectOption(this, '${taskId}', ${i})">${escHtml(opt)}</li>`
  ).join('');
  return `<p>${q}</p>
            <ul class="task-options" id="opts-${taskId}">${opts}</ul>
            <button class="task-submit-btn" onclick="submitSingleChoice('${taskId}')">Проверить</button>`;
}

function selectOption(el, taskId, index) {
  const list = document.getElementById(`opts-${taskId}`);
  if (!list) return;
  list.querySelectorAll('li').forEach(li => li.classList.remove('selected'));
  el.classList.add('selected');
  el.closest('.task-widget').dataset.selected = index;
}

function submitSingleChoice(taskId) {
  const widget = document.querySelector(`.task-widget[data-task-id="${taskId}"]`);
  const selected = widget ? parseInt(widget.dataset.selected) : NaN;
  if (isNaN(selected)) { alert('Выберите вариант ответа'); return; }
  submitAnswer(taskId, { selectedIndex: selected }, function(result) {
    const list = document.getElementById(`opts-${taskId}`);
    if (list) {
      list.querySelectorAll('li').forEach((li, i) => {
        if (i === selected) li.classList.add(result.correct ? 'correct' : 'wrong');
      });
    }
    showFeedback(taskId, result);
  });
}

// ---- Тип: TRUE_FALSE ----
function buildTrueFalseHTML(taskId, c) {
  const stmt = escHtml(c.statement || '');
  return `<p>${stmt}</p>
            <div class="tf-buttons">
              <button class="tf-btn" id="tf-true-${taskId}"  onclick="selectTF('${taskId}', true)">✓ Верно</button>
              <button class="tf-btn" id="tf-false-${taskId}" onclick="selectTF('${taskId}', false)">✗ Неверно</button>
            </div>
            <button class="task-submit-btn" onclick="submitTrueFalse('${taskId}')">Проверить</button>`;
}

function selectTF(taskId, value) {
  const widget = document.querySelector(`.task-widget[data-task-id="${taskId}"]`);
  if (widget) widget.dataset.tfAnswer = value;
  const trueBtn  = document.getElementById(`tf-true-${taskId}`);
  const falseBtn = document.getElementById(`tf-false-${taskId}`);
  if (trueBtn)  trueBtn.classList.toggle('selected', value === true);
  if (falseBtn) falseBtn.classList.toggle('selected', value === false);
}

function submitTrueFalse(taskId) {
  const widget = document.querySelector(`.task-widget[data-task-id="${taskId}"]`);
  if (!widget || widget.dataset.tfAnswer === undefined) { alert('Выберите вариант'); return; }
  const answer = widget.dataset.tfAnswer === 'true';
  submitAnswer(taskId, { answer: answer }, function(result) {
    ['true','false'].forEach(v => {
      const btn = document.getElementById(`tf-${v}-${taskId}`);
      if (!btn) return;
      const isSelected = (v === 'true') === answer;
      if (isSelected) btn.classList.add(result.correct ? 'correct' : 'wrong');
    });
    showFeedback(taskId, result);
  });
}

// ---- Тип: MATCHING ----
function buildMatchingHTML(taskId, c) {
  const left  = c.left  || [];
  const right = c.right || [];
  const rows = left.map((lText, i) => `
      <div style="display:contents">
        <div style="padding:6px 0;font-size:14px">${escHtml(lText)}</div>
        <select class="matching-select" data-left-index="${i}" id="match-${taskId}-${i}">
          <option value="">— выбрать —</option>
          ${right.map((r, j) => `<option value="${j}">${escHtml(r)}</option>`).join('')}
        </select>
      </div>`).join('');
  return `<div class="matching-grid">${rows}</div>
            <button class="task-submit-btn" onclick="submitMatching('${taskId}', ${left.length})">Проверить</button>`;
}

function submitMatching(taskId, count) {
  const pairs = [];
  for (let i = 0; i < count; i++) {
    const sel = document.getElementById(`match-${taskId}-${i}`);
    if (!sel || sel.value === '') { alert('Заполните все пары'); return; }
    pairs.push(parseInt(sel.value));
  }
  submitAnswer(taskId, { pairs: pairs }, function(result) {
    showFeedback(taskId, result);
  });
}

// ---- Тип: FILL_BLANK ----
function buildFillBlankHTML(taskId, c) {
  const sentence = escHtml(c.sentence || '').replace('_', `<input class="fill-blank-input" id="fill-${taskId}" placeholder="ваш ответ" style="width:200px;display:inline-block">`);
  return `<p>${sentence}</p>
            <button class="task-submit-btn" onclick="submitFillBlank('${taskId}')">Проверить</button>`;
}

function submitFillBlank(taskId) {
  const input = document.getElementById(`fill-${taskId}`);
  if (!input || !input.value.trim()) { alert('Введите ответ'); return; }
  submitAnswer(taskId, { answer: input.value.trim() }, function(result) {
    if (input) input.classList.add(result.correct ? 'correct' : 'wrong');
    showFeedback(taskId, result);
  });
}

// ---- Тип: ORDERING ----
function buildOrderingHTML(taskId, c) {
  const items = (c.items || []).slice(); // Copy to avoid mutation
  const rows = items.map((item, i) =>
      `<li class="ordering-item" data-original-index="${i}" id="ord-item-${taskId}-${i}">
           <button onclick="moveOrderItem('${taskId}', this, -1)" title="Вверх">▲</button>
           <button onclick="moveOrderItem('${taskId}', this, 1)"  title="Вниз">▼</button>
           <span>${escHtml(item)}</span>
         </li>`
  ).join('');
  return `<ul class="ordering-list" id="ord-list-${taskId}">${rows}</ul>
            <button class="task-submit-btn" onclick="submitOrdering('${taskId}')">Проверить</button>`;
}

function moveOrderItem(taskId, btn, direction) {
  const item = btn.closest('.ordering-item');
  const list = item.parentElement;
  const items = Array.from(list.querySelectorAll('.ordering-item'));
  const pos = items.indexOf(item);
  const targetPos = pos + direction;
  if (targetPos < 0 || targetPos >= items.length) return;
  if (direction > 0) list.insertBefore(items[targetPos], item);
  else list.insertBefore(item, items[targetPos]);
}

function submitOrdering(taskId) {
  const list = document.getElementById(`ord-list-${taskId}`);
  if (!list) return;
  // Collect the original indices in current displayed order — this IS the user's answer
  const order = Array.from(list.querySelectorAll('.ordering-item'))
      .map(el => parseInt(el.dataset.originalIndex));
  submitAnswer(taskId, { order: order }, function(result) {
    showFeedback(taskId, result);
  });
}

// ---- Тип: OPEN_QUESTION ----
function buildOpenQuestionHTML(taskId, c) {
  const q = escHtml(c.question || '');
  const hint = c.hint ? `<p style="font-size:13px;color:#5F6368;margin-top:6px">💡 Подсказка: ${escHtml(c.hint)}</p>` : '';
  return `<p>${q}</p>${hint}
            <textarea class="open-answer-area" id="open-${taskId}" placeholder="Напишите ваш ответ..."></textarea>
            <button class="task-submit-btn" onclick="submitOpenQuestion('${taskId}')">Отправить на проверку</button>`;
}

function submitOpenQuestion(taskId) {
  const textarea = document.getElementById(`open-${taskId}`);
  if (!textarea || !textarea.value.trim()) { alert('Напишите ответ'); return; }
  const btn = textarea.closest('.task-widget').querySelector('.task-submit-btn');
  if (btn) { btn.disabled = true; btn.textContent = 'Проверяю...'; }
  submitAnswer(taskId, { answer: textarea.value.trim() }, function(result) {
    if (btn) { btn.disabled = false; btn.textContent = 'Отправить на проверку'; }
    showFeedback(taskId, result, 'info');
  });
}

// ---- Тип: PRACTICE / CODE_SNIPPET ----
function buildPracticeHTML(taskId, c, type) {
  if (type === 'CODE_SNIPPET') {
    return `<p>${escHtml(c.question || '')}</p>
                <pre><code>${escHtml(c.code || '')}</code></pre>
                <textarea class="open-answer-area" id="open-${taskId}" placeholder="Ваш ответ..."></textarea>
                <button class="task-submit-btn" onclick="submitOpenQuestion('${taskId}')">Отправить</button>`;
  }
  return `<p>${escHtml(c.description || '')}</p>
            <textarea class="open-answer-area" id="open-${taskId}" placeholder="Ваш ответ..."></textarea>
            <button class="task-submit-btn" onclick="submitOpenQuestion('${taskId}')">Отправить</button>`;
}

// ============================================================
// ОБЩАЯ ЛОГИКА ПРОВЕРКИ
// ============================================================

/**
 * Отправляет ответ на сервер и вызывает callback с результатом.
 */
function submitAnswer(taskId, answerPayload, callback) {
  fetch(`/api/v1/roadmap/task/${taskId}/check`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ answer: answerPayload })
  })
      .then(res => {
        if (!res.ok) throw new Error('Ошибка сервера: ' + res.status);
        return res.json();
      })
      .then(data => callback(data))
      .catch(err => {
        console.error('Ошибка проверки:', err);
        alert('Не удалось проверить ответ. Попробуйте ещё раз.');
      });
}

/**
 * Отображает блок обратной связи под задачей.
 */
function showFeedback(taskId, result, forceClass) {
  const el = document.getElementById(`feedback-${taskId}`);
  if (!el) return;
  const cssClass = forceClass || (result.correct ? 'correct' : 'wrong');
  el.className = `task-feedback ${cssClass}`;
  el.textContent = result.explanation || (result.correct ? '✓ Истина!' : '✗ Ложь');
  el.style.display = 'block';
}

// ============================================================
// ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
// ============================================================

function badgeClass(type) {
  switch (type) {
    case 'SINGLE_CHOICE':return 'badge-choice';
    case 'TRUE_FALSE':   return 'badge-tf';
    case 'MATCHING':     return 'badge-match';
    case 'FILL_BLANK':   return 'badge-fill';
    case 'ORDERING':     return 'badge-order';
    case 'OPEN_QUESTION':return 'badge-open';
    default:             return 'badge-practice';
  }
}

function badgeLabel(type) {
  switch (type) {
    case 'SINGLE_CHOICE': return 'Выбор ответа';
    case 'TRUE_FALSE':    return 'Верно/Неверно';
    case 'MATCHING':      return 'Сопоставление';
    case 'FILL_BLANK':    return 'Пропуск';
    case 'ORDERING':      return 'Порядок';
    case 'OPEN_QUESTION': return 'Открытый вопрос';
    case 'CODE_SNIPPET':  return 'Анализ кода';
    default:              return 'Практика';
  }
}

function escHtml(str) {
  if (!str) return '';
  return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
}

// ============================================================
// ПОЛЛИНГ СТАТУСА ЧЕКПОИНТА
// ============================================================

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
      console.warn("Ошибка при опросе статуса (сеть), попробую снова через 5 сек...", err);
    }
  }, 5000);
}

// ============================================================
// УГЛУБЛЕНИЕ ТЕМЫ
// ============================================================

/**
 * Запрос на генерацию дочернего чекпоинта (углубление темы)
 */
function requestDeepen(checkpointId) {
  const userPrompt = prompt("Какую подтему вы хотите разобрать подробнее?");
  if (!userPrompt || userPrompt.trim() === "") return;

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