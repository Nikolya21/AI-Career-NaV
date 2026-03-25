<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.commonmark.node.*" %>
<%@ page import="org.commonmark.parser.Parser" %>
<%@ page import="org.commonmark.renderer.html.HtmlRenderer" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">
    <style>
      .progress-indicator {
        background: white;
        padding: 15px 20px;
        border-radius: 10px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        margin-bottom: 15px;
      }

      .progress-text {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
        text-align: center;
      }

      .progress-bar {
        background: #e9ecef;
        border-radius: 10px;
        height: 8px;
        overflow: hidden;
      }

      .progress-fill {
        background: linear-gradient(135deg, #007BFF, #0056b3);
        height: 100%;
        border-radius: 10px;
        transition: width 0.3s ease;
      }

      /* Стили для Markdown контента в сообщениях AI */
      .ai-message .message-content {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
        line-height: 1.6;
        color: #333;
      }

      .ai-message .message-content h1,
      .ai-message .message-content h2,
      .ai-message .message-content h3,
      .ai-message .message-content h4 {
        color: #2c3e50;
        margin-top: 1.2em;
        margin-bottom: 0.5em;
        font-weight: 600;
      }

      .ai-message .message-content h1 {
        font-size: 1.4em;
        border-bottom: 2px solid #3498db;
        padding-bottom: 0.3em;
      }

      .ai-message .message-content h2 {
        font-size: 1.2em;
        border-bottom: 1px solid #eee;
        padding-bottom: 0.3em;
      }

      .ai-message .message-content h3 {
        font-size: 1.1em;
      }

      .ai-message .message-content p {
        margin-bottom: 1em;
      }

      .ai-message .message-content ul,
      .ai-message .message-content ol {
        margin-bottom: 1em;
        padding-left: 1.5em;
      }

      .ai-message .message-content li {
        margin-bottom: 0.5em;
      }

      .ai-message .message-content code {
        background-color: #f8f9fa;
        padding: 0.2em 0.4em;
        border-radius: 3px;
        font-family: 'Courier New', monospace;
        font-size: 0.9em;
      }

      .ai-message .message-content pre {
        background-color: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 6px;
        padding: 1em;
        overflow-x: auto;
        margin: 1em 0;
      }

      .ai-message .message-content pre code {
        background-color: transparent;
        padding: 0;
      }

      .ai-message .message-content blockquote {
        border-left: 4px solid #3498db;
        padding-left: 1em;
        margin: 1em 0;
        color: #555;
        font-style: italic;
      }

      .ai-message .message-content a {
        color: #3498db;
        text-decoration: none;
      }

      .ai-message .message-content a:hover {
        text-decoration: underline;
      }

      .ai-message .message-content strong {
        font-weight: 600;
        color: #2c3e50;
      }

      .ai-message .message-content em {
        font-style: italic;
      }

      /* Дополнительные стили для сообщений пользователя */
      .user-message .message-content {
        white-space: pre-wrap;
        word-wrap: break-word;
      }
    </style>
</head>
<body>
<%
    String selectedVacancy = (String) request.getAttribute("selectedVacancy");
    if (selectedVacancy != null) {
%>
<div class="selected-vacancy-info"
     style="background: #e7f3ff; padding: 15px; border-radius: 10px; margin-bottom: 20px; border-left: 4px solid #007BFF;">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
            <strong>🎯 Целевая вакансия:</strong> <%= selectedVacancy %>
        </div>
        <a href="${pageContext.request.contextPath}/real-vacancies"
           style="background: #28a745; color: white; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 14px;">
            📊 Показать реальные вакансии
        </a>
    </div>
</div>
<% } %>

<div class="header">
    <div class="header-left">
        <a href="${pageContext.request.contextPath}/personal-cabinet" class="cabinet-btn">
            👤 Личный кабинет
        </a>
    </div>

    <h1>Career Navigator</h1>

    <div class="user-info">
        <%
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail != null) {
        %>
        <span>Вы вошли как: <%= userEmail %></span>
        <a href="${pageContext.request.contextPath}/logout" class="logout-btn">Выйти</a>
        <% } %>
    </div>
</div>

<div class="dialog-container">
    <%-- Индикатор прогресса --%>
    <%
        Integer questionsCount = (Integer) request.getAttribute("questionsCount");
        Boolean dialogCompleted = (Boolean) request.getAttribute("dialogCompleted");

        if (questionsCount != null && questionsCount > 0 && (dialogCompleted == null
                || !dialogCompleted)) {
    %>
    <div class="progress-indicator">
        <div class="progress-text">
            Вопрос <%= questionsCount %> из 5
        </div>
        <div class="progress-bar">
            <div class="progress-fill" style="width: <%= questionsCount * 20 %>%;"></div>
        </div>
    </div>
    <% } %>

    <div class="dialog-history" id="chatHistory">
        <%
            // Получаем историю диалога из request или session
            List<String> discussionHistory = (List<String>) request.getAttribute(
                    "discussionHistory");
            if (discussionHistory == null) {
                discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
            }

            if (discussionHistory == null) {
                discussionHistory = new ArrayList<>();
            }

            // Если история пустая, показываем сообщение по умолчанию
            if (discussionHistory.isEmpty()) {
        %>
        <div class="message ai-message">
            <div class="message-sender">🤖 AI</div>
            <div class="message-content">
                Начните диалог, нажав кнопку "Начать диалог" в личном кабинете.
            </div>
        </div>
        <%
        } else {
            // Отображаем все сообщения из истории
            for (String message : discussionHistory) {
                if (message.startsWith("User:")) {
        %>
        <div class="message user-message">
            <div class="message-sender">👤 Вы</div>
            <div class="message-content">
                <%= message.substring(5).trim() %>
            </div>
        </div>
        <%
        } else if (message.startsWith("AI:")) {
            String aiMessage = message.substring(3).trim();
            String markdownHtml = convertMarkdownToHtml(aiMessage);
        %>
        <div class="message ai-message">
            <div class="message-sender">🤖 AI</div>
            <div class="message-content">
                <%= markdownHtml %>
            </div>
        </div>
        <%
        } else {
            // Сообщение без префикса (считаем AI)
            String markdownHtml = convertMarkdownToHtml(message);
        %>
        <div class="message ai-message">
            <div class="message-sender">🤖 AI</div>
            <div class="message-content">
                <%= markdownHtml %>
            </div>
        </div>
        <%
                    }
                }
            }
        %>
    </div>

    <%
        // Проверяем, не завершен ли диалог
        if (dialogCompleted == null || !dialogCompleted) {
    %>
    <form action="${pageContext.request.contextPath}/vacancy-discussion" method="post"
          class="message-form" id="messageForm">
        <input type="text" name="message" placeholder="Type your message here..."
               class="message-input" id="messageInput" required>
        <button type="submit" class="btn-send">Send</button>
    </form>
    <%
    } else {
    %>
    <div class="dialog-completed-message">
        <div class="completion-info">
            <h3>Диалог завершен</h3>
            <p>Вы достигли лимита в 5 вопросов. <a
                    href="${pageContext.request.contextPath}/dialog-completed">Посмотреть итоги</a>
            </p>
        </div>
    </div>
    <% } %>
</div>


<script>
  // Автопрокрутка к последнему сообщению
  function scrollToBottom() {
    const chatHistory = document.getElementById('chatHistory');
    if (chatHistory) {
      chatHistory.scrollTop = chatHistory.scrollHeight;
    }
  }

  // Прокрутка при загрузке страницы
  document.addEventListener('DOMContentLoaded', function () {
    scrollToBottom();

    // Очистка поля ввода после отправки
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');

    if (messageForm && messageInput) {
      messageForm.addEventListener('submit', function (e) {
        if (messageInput.value.trim() !== '') {
          console.log('Sending message:', messageInput.value);
        }
      });
    }
  });

  // Фокус на поле ввода
  window.onload = function () {
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
      messageInput.focus();
    }
  };
</script>
</body>
</html>

<%!
    // Вспомогательный метод для конвертации Markdown в HTML
    private String convertMarkdownToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }

        try {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdown);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document);
        } catch (Exception e) {
            // Если произошла ошибка, возвращаем текст как есть с базовой обработкой
            return escapeHtml(markdown)
                    .replace("\n", "<br>")
                    .replace("### ", "<h3>").replace("\n", "</h3>")
                    .replace("## ", "<h2>").replace("\n", "</h2>")
                    .replace("# ", "<h1>").replace("\n", "</h1>")
                    .replace("**", "<strong>").replace("**", "</strong>")
                    .replace("*", "<em>").replace("*", "</em>")
                    .replace("`", "<code>").replace("`", "</code>")
                    .replace("```", "<pre><code>").replace("```", "</code></pre>");
        }
    }

    // Метод для экранирования HTML
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>