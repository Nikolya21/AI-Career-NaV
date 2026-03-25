<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Диалог завершен - Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">
    <style>
      .completion-container {
        text-align: center;
        padding: 40px 20px;
      }

      .completion-icon {
        font-size: 64px;
        margin-bottom: 20px;
      }

      .completion-message {
        background: white;
        padding: 30px;
        border-radius: 15px;
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
        margin-bottom: 30px;
      }

      .stats {
        display: flex;
        justify-content: center;
        gap: 30px;
        margin: 20px 0;
      }

      .stat-item {
        background: #f8f9fa;
        padding: 15px 25px;
        border-radius: 10px;
        border: 1px solid #e9ecef;
      }

      .stat-number {
        font-size: 24px;
        font-weight: bold;
        color: #007BFF;
      }

      .stat-label {
        font-size: 14px;
        color: #666;
      }

      .actions {
        margin-top: 30px;
      }

      .btn-new-dialog {
        padding: 15px 30px;
        background: linear-gradient(135deg, #28a745, #20c997);
        color: white;
        border: none;
        border-radius: 12px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-block;
      }

      .btn-new-dialog:hover {
        background: linear-gradient(135deg, #20c997, #1e9c7a);
        transform: translateY(-2px);
        box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
      }
    </style>
</head>
<body>
<div class="header">
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
    <div class="completion-container">
        <div class="completion-icon">🎯</div>

        <div class="completion-message">
            <h2>Диалог успешно завершен!</h2>
            <p>Вы задали 5 вопросов и получили ценные рекомендации по вашему карьерному развитию.</p>

            <div class="stats">
                <div class="stat-item">
                    <div class="stat-number">
                        <%
                            Integer questionsCount = (Integer) request.getAttribute("questionsCount");
                            out.print(questionsCount != null ? questionsCount : 5);
                        %>
                    </div>
                    <div class="stat-label">вопросов задано</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">
                        <%
                            List<String> history = (List<String>) request.getAttribute("messageHistory");
                            out.print(history != null ? history.size() : 10);
                        %>
                    </div>
                    <div class="stat-label">сообщений в диалоге</div>
                </div>
            </div>
        </div>

        <div class="actions">
            <form action="${pageContext.request.contextPath}/choose-vacancy" class="btn-new-dialog">
                <button type="submit" class="btn-new-dialog">Перейти к выбору вакансии</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>