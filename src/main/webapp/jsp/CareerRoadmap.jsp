<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ page import="org.example.aicareernav1.entity.roadmapEntity.RoadmapTaskEntity" %>
<%@ page import="org.example.aicareernav1.entity.roadmapEntity.RoadmapWeekEntity" %>
<%@ page import="org.example.aicareernav1.model.courseModel.Week" %>
<%@ page import="org.example.aicareernav1.model.courseModel.Task" %>
<%@ page import="java.util.List" %>
<%@ page import="org.commonmark.node.*" %>
<%@ page import="org.commonmark.parser.Parser" %>
<%@ page import="org.commonmark.renderer.html.HtmlRenderer" %>
<%@ page import="org.example.aicareernav1.model.roadmap.RoadmapZone" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ваш карьерный план - AI Career Navigator</title>
    <style>
      .markdown-content {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
        line-height: 1.6;
        color: #333;
      }

      .markdown-content h1,
      .markdown-content h2,
      .markdown-content h3,
      .markdown-content h4 {
        color: #2c3e50;
        margin-top: 1.5em;
        margin-bottom: 0.5em;
        font-weight: 600;
      }

      .markdown-content h1 {
        font-size: 1.8em;
        border-bottom: 2px solid #3498db;
        padding-bottom: 0.3em;
      }

      .markdown-content h2 {
        font-size: 1.5em;
        border-bottom: 1px solid #eee;
        padding-bottom: 0.3em;
      }

      .markdown-content h3 {
        font-size: 1.3em;
      }

      .markdown-content h4 {
        font-size: 1.1em;
      }

      .markdown-content p {
        margin-bottom: 1em;
      }

      .markdown-content ul,
      .markdown-content ol {
        margin-bottom: 1em;
        padding-left: 2em;
      }

      .markdown-content li {
        margin-bottom: 0.5em;
      }

      .markdown-content code {
        background-color: #f8f9fa;
        padding: 0.2em 0.4em;
        border-radius: 3px;
        font-family: 'Courier New', monospace;
        font-size: 0.9em;
      }

      .markdown-content pre {
        background-color: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 6px;
        padding: 1em;
        overflow-x: auto;
        margin: 1em 0;
      }

      .markdown-content pre code {
        background-color: transparent;
        padding: 0;
      }

      .markdown-content blockquote {
        border-left: 4px solid #3498db;
        padding-left: 1em;
        margin: 1em 0;
        color: #555;
        font-style: italic;
      }

      .markdown-content a {
        color: #3498db;
        text-decoration: none;
      }

      .markdown-content a:hover {
        text-decoration: underline;
      }

      .markdown-content strong {
        font-weight: 600;
        color: #2c3e50;
      }

      .markdown-content em {
        font-style: italic;
      }

      .markdown-content table {
        border-collapse: collapse;
        width: 100%;
        margin: 1em 0;
      }

      .markdown-content th,
      .markdown-content td {
        border: 1px solid #dee2e6;
        padding: 0.75em;
        text-align: left;
      }

      .markdown-content th {
        background-color: #f8f9fa;
        font-weight: 600;
      }

      /* Дополнительные стили для улучшения читаемости */
      .personalized-section .markdown-content {
        background: white;
        border-radius: 8px;
        padding: 20px;
        border: 1px solid #d4edda;
      }

      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        min-height: 100vh;
        padding: 20px;
      }

      .container {
        max-width: 1200px;
        margin: 0 auto;
        background: white;
        border-radius: 15px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        overflow: hidden;
      }

      .header {
        background: linear-gradient(135deg, #2c3e50, #34495e);
        color: white;
        padding: 30px;
        text-align: center;
      }

      .header h1 {
        font-size: 2.5em;
        margin-bottom: 10px;
      }

      .vacancy-info {
        font-size: 1.2em;
        opacity: 0.9;
        margin-bottom: 20px;
      }

      .roadmap-content {
        padding: 30px;
      }

      .error-message {
        background: #ffeaa7;
        border: 1px solid #fdcb6e;
        border-radius: 10px;
        padding: 20px;
        margin: 20px 0;
        text-align: center;
      }

      .error-message h3 {
        color: #e74c3c;
        margin-bottom: 10px;
      }

      /* Стили для персонализированной информации */
      .personalized-section {
        background: linear-gradient(135deg, #e8f5e8, #d4edda);
        border: 1px solid #c3e6cb;
        border-radius: 10px;
        padding: 25px;
        margin: 20px 0;
      }

      .personalized-section h3 {
        color: #155724;
        margin-bottom: 15px;
        display: flex;
        align-items: center;
        gap: 10px;
      }

      .personalized-section p {
        color: #0f5132;
        line-height: 1.6;
        margin-bottom: 10px;
      }

      .dialog-summary {
        background: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 8px;
        padding: 15px;
        margin: 15px 0;
      }

      .dialog-summary h4 {
        color: #495057;
        margin-bottom: 10px;
      }

      /* Стили для зон */
      .zone {
        background: #f8f9fa;
        border-radius: 10px;
        margin-bottom: 25px;
        border-left: 5px solid #3498db;
        overflow: hidden;
      }

      .zone-header {
        background: #ecf0f1;
        padding: 20px;
        cursor: pointer;
        transition: background 0.3s ease;
      }

      .zone-header:hover {
        background: #d5dbdb;
      }

      .zone-title {
        font-size: 1.4em;
        color: #2c3e50;
        margin-bottom: 8px;
      }

      .zone-meta {
        display: flex;
        gap: 20px;
        font-size: 0.9em;
        color: #7f8c8d;
      }

      .zone-content {
        padding: 0 20px;
        max-height: 0;
        overflow: hidden;
        transition: max-height 0.3s ease, padding 0.3s ease;
      }

      .zone-content.expanded {
        padding: 20px;
        max-height: 5000px;
      }

      /* Стили для недель */
      .week {
        background: white;
        border-radius: 8px;
        padding: 15px;
        margin: 10px 0;
        border: 1px solid #bdc3c7;
      }

      .week-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
      }

      .week-number {
        font-weight: bold;
        color: #e74c3c;
        font-size: 1.1em;
      }

      .week-goal {
        color: #2c3e50;
        font-style: italic;
      }

      /* Стили для задач */
      .tasks-list {
        margin-top: 10px;
      }

      .task {
        background: #f1f2f6;
        padding: 12px;
        margin: 8px 0;
        border-radius: 6px;
        border-left: 3px solid #27ae60;
      }

      .task-description {
        margin-bottom: 8px;
      }

      .task-links {
        font-size: 0.9em;
      }

      .task-links a {
        color: #3498db;
        text-decoration: none;
        margin-right: 10px;
        display: inline-block;
        padding: 2px 6px;
        background: #e3f2fd;
        border-radius: 4px;
      }

      .task-links a:hover {
        text-decoration: underline;
        background: #bbdefb;
      }

      /* Навигация */
      .navigation {
        text-align: center;
        padding: 20px;
        background: #ecf0f1;
        border-top: 1px solid #bdc3c7;
      }

      .btn {
        display: inline-block;
        padding: 12px 25px;
        background: linear-gradient(135deg, #3498db, #2980b9);
        color: white;
        text-decoration: none;
        border-radius: 8px;
        margin: 0 10px;
        transition: transform 0.2s ease;
      }

      .btn:hover {
        transform: translateY(-2px);
      }

      .btn-secondary {
        background: linear-gradient(135deg, #95a5a6, #7f8c8d);
      }

      .progress-indicator {
        background: #34495e;
        color: white;
        padding: 10px;
        text-align: center;
        font-size: 0.9em;
      }

      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #7f8c8d;
      }

      .empty-state h3 {
        margin-bottom: 15px;
        color: #2c3e50;
      }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🎯 Ваш персонализированный карьерный план</h1>
        <div class="vacancy-info">
            <%
                String selectedVacancy = (String) request.getAttribute("selectedVacancy");
                if (selectedVacancy != null) {
            %>
            Целевая вакансия: <strong><%= selectedVacancy %></strong>
            <% } %>
        </div>
    </div>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="error-message">
        <h3>⚠️ Внимание</h3>
        <p><%= error %></p>
    </div>
    <% } %>

    <%-- ✅ ОТОБРАЖЕНИЕ ПЕРСОНАЛИЗИРОВАННОЙ ИНФОРМАЦИИ С MARKDOWN --%>
    <%
        String personalizedPlan = (String) session.getAttribute("personalizedVacancyPlan");
        String fullDiscussionPrompt = (String) session.getAttribute("fullDiscussionPrompt");

        if (personalizedPlan != null && !personalizedPlan.trim().isEmpty()) {
            // Конвертируем Markdown в HTML
            String markdownHtml = convertMarkdownToHtml(personalizedPlan);
    %>
    <div class="personalized-section">
        <h3>📝 Ваш персонализированный план</h3>
        <div class="markdown-content">
            <%= markdownHtml %>
        </div>

        <% if (fullDiscussionPrompt != null && fullDiscussionPrompt.length() > 500) { %>
        <div class="dialog-summary">
            <h4>ℹ️ Основано на вашем диалоге</h4>
            <p>Этот план создан на основе нашего обсуждения, где вы поделились своими целями, опытом и предпочтениями.</p>
            <p><small>Всего вопросов в диалоге: 5 | Направление: <%= selectedVacancy %></small></p>
        </div>
        <% } %>
    </div>
    <% } %>

    <%
        Roadmap roadmap = (Roadmap) request.getAttribute("roadmap");
        if (roadmap != null && roadmap.getRoadmapZones() != null && !roadmap.getRoadmapZones().isEmpty()) {
    %>
    <div class="progress-indicator">
        Всего этапов: <%= roadmap.getRoadmapZones().size() %> |
        Общая продолжительность: ~<%= calculateTotalWeeks(roadmap) %> недель |
        💡 План создан на основе ваших ответов
    </div>

    <div class="roadmap-content">
        <%
            for (RoadmapZone zone : roadmap.getRoadmapZones()) {
        %>
        <div class="zone">
            <div class="zone-header" onclick="toggleZone(this)">
                <div class="zone-title">
                    <%= zone.getName() != null ? zone.getName() : "Этап " + zone.getZoneOrder() %>
                </div>
                <div class="zone-meta">
                    <span>📅 Недели: <%= getWeeksRange(zone) %></span>
                    <span>⚡ Сложность: <%= zone.getComplexityLevel() != null ? zone.getComplexityLevel() : "Средняя" %></span>
                    <span>🎯 Цель: <%= zone.getLearningGoal() != null ? zone.getLearningGoal() : "Развитие навыков" %></span>
                </div>
            </div>
            <div class="zone-content">
                <%
                    if (zone.getWeeks() != null && !zone.getWeeks().isEmpty()) {
                        for (Week week : zone.getWeeks()) {
                %>
                <div class="week">
                    <div class="week-header">
                        <span class="week-number">Неделя <%= week.getNumber() %></span>
                        <span class="week-goal"><%= week.getGoal() != null ? week.getGoal() : "" %></span>
                    </div>
                    <%
                        if (week.getTasks() != null && !week.getTasks().isEmpty()) {
                    %>
                    <div class="tasks-list">
                        <%
                            for (Task task : week.getTasks()) {
                        %>
                        <div class="task">
                            <div class="task-description">
                                <%= task.getDescription() != null ? task.getDescription() : "Задача недели" %>
                            </div>
                            <%
                                if (task.getUrls() != null && !task.getUrls().isEmpty()) {
                            %>
                            <div class="task-links">
                                <strong>Ресурсы:</strong>
                                <%
                                    for (String url : task.getUrls()) {
                                %>
                                <a href="<%= url %>" target="_blank" rel="noopener noreferrer">
                                    <%= getDomainFromUrl(url) %>
                                </a>
                                <% } %>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                    <% } else { %>
                    <p style="color: #7f8c8d; font-style: italic;">Задачи для этой недели находятся в разработке...</p>
                    <% } %>
                </div>
                <% } %>
                <% } else { %>
                <div class="empty-state">
                    <p>План для этой зоны находится в разработке...</p>
                </div>
                <% } %>
            </div>
        </div>
        <% } %>
    </div>
    <% } else { %>
    <div class="empty-state">
        <h3>🚧 Карьерный план еще не готов</h3>
        <p>Ваш персонализированный план развития находится в процессе создания.</p>
        <p>Пожалуйста, вернитесь позже или обратитесь в поддержку.</p>
    </div>
    <% } %>

    <div class="navigation">
        <a href="${pageContext.request.contextPath}/personal-cabinet" class="btn btn-secondary">
            👤 Личный кабинет
        </a>
        <a href="${pageContext.request.contextPath}/vacancy-discussion" class="btn">
            💬 Вернуться к обсуждению
        </a>
    </div>
</div>

<script>
  function toggleZone(header) {
    const content = header.nextElementSibling;
    const isExpanded = content.classList.contains('expanded');

    // Закрываем все открытые зоны
    document.querySelectorAll('.zone-content.expanded').forEach(expandedContent => {
      if (expandedContent !== content) {
        expandedContent.classList.remove('expanded');
      }
    });

    // Переключаем текущую зону
    if (!isExpanded) {
      content.classList.add('expanded');
    }
  }

  // Автоматически открываем первую зону
  document.addEventListener('DOMContentLoaded', function() {
    const firstZoneHeader = document.querySelector('.zone-header');
    if (firstZoneHeader) {
      firstZoneHeader.click();
    }
  });
</script>
</body>
</html>

<%!
    // Вспомогательные методы для JSP

    // Метод для конвертации Markdown в HTML
    private String convertMarkdownToHtml(String markdown) {
        try {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdown);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document);
        } catch (Exception e) {
            // Если произошла ошибка, возвращаем текст как есть с базовой обработкой
            return escapeHtml(markdown)
                    .replace("\n", "<br>")
                    .replace("\\*\\*", "<strong>")
                    .replace("\\*", "<em>");
        }
    }

    // Метод для экранирования HTML
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private int calculateTotalWeeks(Roadmap roadmap) {
        if (roadmap.getRoadmapZones() == null) return 0;
        int totalWeeks = 0;
        for (RoadmapZone zone : roadmap.getRoadmapZones()) {
            if (zone.getWeeks() != null) {
                totalWeeks += zone.getWeeks().size();
            }
        }
        return totalWeeks;
    }

    private String getWeeksRange(RoadmapZone zone) {
        if (zone.getWeeks() == null || zone.getWeeks().isEmpty()) return "0-0";

        int firstWeek = zone.getWeeks().get(0).getNumber();
        int lastWeek = zone.getWeeks().get(zone.getWeeks().size() - 1).getNumber();

        return firstWeek + "-" + lastWeek;
    }

    private String getDomainFromUrl(String url) {
        try {
            if (url.contains("://")) {
                String domain = url.split("://")[1].split("/")[0];
                return domain.startsWith("www.") ? domain.substring(4) : domain;
            }
            return "ссылка";
        } catch (Exception e) {
            return "ресурс";
        }
    }
%>