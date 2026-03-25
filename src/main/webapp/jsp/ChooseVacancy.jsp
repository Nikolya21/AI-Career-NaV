<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Выбор вакансии - Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">
    <style>
      .vacancy-container {
        max-width: 800px;
        margin: 0 auto;
        padding: 20px;
      }

      .vacancy-header {
        text-align: center;
        margin-bottom: 30px;
      }

      .vacancy-options {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 20px;
        margin-bottom: 25px;
      }

      .vacancy-option {
        position: relative;
      }

      .vacancy-option input[type="radio"] {
        display: none;
      }

      .vacancy-label {
        display: block;
        padding: 25px 20px;
        background: #f8f9fa;
        border: 2px solid #e1e8ed;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s ease;
        text-align: center;
      }

      .vacancy-label:hover {
        background: #e9ecef;
        border-color: #007BFF;
        transform: translateY(-2px);
      }

      .vacancy-option input[type="radio"]:checked + .vacancy-label {
        background: linear-gradient(135deg, #007BFF, #0056b3);
        color: white;
        border-color: #007BFF;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 123, 255, 0.4);
      }

      .vacancy-title {
        font-weight: 600;
        font-size: 18px;
        margin-bottom: 8px;
      }

      .vacancy-number {
        position: absolute;
        top: 10px;
        left: 10px;
        background: #007BFF;
        color: white;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: bold;
      }

      .vacancy-option input[type="radio"]:checked + .vacancy-label .vacancy-number {
        background: white;
        color: #007BFF;
      }

      .submit-btn {
        width: 100%;
        padding: 16px;
        background: linear-gradient(135deg, #ff8c00, #ff6b35);
        color: white;
        border: none;
        border-radius: 12px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        margin-top: 20px;
      }

      .submit-btn:hover {
        background: linear-gradient(135deg, #ff6b35, #ff5733);
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(255, 107, 53, 0.4);
      }

      .error-message {
        background: #ffeaea;
        color: #d63031;
        padding: 15px;
        border-radius: 8px;
        margin-bottom: 20px;
        border: 1px solid #ffcccc;
        text-align: center;
      }

      .info-box {
        background: #e7f3ff;
        border: 1px solid #b3d9ff;
        border-radius: 8px;
        padding: 15px;
        margin-bottom: 20px;
      }
    </style>
</head>
<body>
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
    <div class="vacancy-container">
        <div class="vacancy-header">
            <h2>Выбор целевой вакансии</h2>
            <p>На основе вашего диалога мы подобрали 3 наиболее подходящие вакансии</p>
        </div>

        <%-- Сообщение об ошибке --%>
        <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
        %>
        <div class="error-message">
            <%= error %>
        </div>
        <% } %>

        <div class="info-box">
            <h4>💡 Рекомендация</h4>
            <p>Выберите одну из предложенных вакансий. Мы создадим персонализированный план развития именно для этой профессии.</p>
        </div>

        <%-- Список вакансий --%>
        <%
            List<String> suggestedVacancies = (List<String>) request.getAttribute("suggestedVacancies");
            if (suggestedVacancies != null && !suggestedVacancies.isEmpty()) {
        %>
        <form action="${pageContext.request.contextPath}/choose-vacancy" method="post">
            <div class="vacancy-options">
                <%
                    for (int i = 0; i < suggestedVacancies.size(); i++) {
                        String vacancy = suggestedVacancies.get(i);
                %>
                <div class="vacancy-option">
                    <input type="radio" id="vacancy<%= i %>" name="selectedVacancy" value="<%= vacancy %>" required>
                    <label for="vacancy<%= i %>" class="vacancy-label">
                        <div class="vacancy-number"><%= i + 1 %></div>
                        <div class="vacancy-title"><%= vacancy %></div>
                    </label>
                </div>
                <% } %>
            </div>
            <button type="submit" class="submit-btn">
                🚀 Начать подготовку к выбранной вакансии
            </button>
        </form>
        <% } else { %>
        <div class="error-message">
            Не удалось подобрать вакансии. Пожалуйста, пройдите диалог заново.
        </div>
        <% } %>
    </div>
</div>

<script>
  document.addEventListener('DOMContentLoaded', function() {
    const submitBtn = document.querySelector('.submit-btn');
    const radioButtons = document.querySelectorAll('input[type="radio"][name="selectedVacancy"]');

    // Блокировка кнопки пока не выбран вариант
    if (submitBtn) {
      submitBtn.disabled = true;

      radioButtons.forEach(radio => {
        radio.addEventListener('change', function() {
          submitBtn.disabled = false;
        });
      });
    }
  });
</script>
</body>
</html>