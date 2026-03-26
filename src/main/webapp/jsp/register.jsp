<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Регистрация - AI Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Register.css">
</head>
<body>
<div class="register-container">
    <h2>Регистрация</h2>

    <%-- Сообщение об ошибках --%>
    <%
        List<String> errors = (List<String>) request.getAttribute("errors");
        String prefilledEmail = (String) request.getAttribute("email");
        String prefilledName = (String) request.getAttribute("name");

        if (errors != null && !errors.isEmpty()) {
    %>
    <div class="error-message">
        <strong>Ошибки:</strong>
        <ul>
            <% for (String error : errors) { %>
            <li><%= error %></li>
            <% } %>
        </ul>
    </div>
    <% } %>

    <form action="${pageContext.request.contextPath}/register" method="post" class="register-form" id="registerForm">
        <div class="input-group">
            <input type="text"
                   name="name"
                   value="<%= prefilledName != null ? prefilledName : "" %>"
                   placeholder="Имя"
                   required
                   minlength="2"
                   maxlength="100"
                   pattern="[A-Za-zА-Яа-яЁё\s]+"
                   title="Только буквы и пробелы">
            <small class="hint">Только буквы и пробелы (мин. 2 символа)</small>
        </div>

        <div class="input-group">
            <input type="email"
                   name="email"
                   value="<%= prefilledEmail != null ? prefilledEmail : "" %>"
                   placeholder="Электронная почта"
                   required
                   maxlength="255">
            <small class="hint">Введите действительный email адрес</small>
        </div>

        <div class="input-group">
            <input type="password"
                   name="password"
                   placeholder="Пароль"
                   required
                   minlength="6"
                   maxlength="100"
                   pattern="^(?=.*[A-Za-z])(?=.*\d).{6,}$"
                   title="Минимум 6 символов, включая буквы и цифры">
            <small class="hint">Минимум 6 символов, включая буквы и цифры</small>
        </div>

        <div class="input-group">
            <input type="password"
                   name="confirmPassword"
                   placeholder="Подтвердите пароль"
                   required
                   minlength="6"
                   maxlength="100">
            <small class="hint">Повторите пароль для подтверждения</small>
        </div>

        <button type="submit" class="btn-register">Зарегистрироваться</button>

        <div class="login-link">
            Уже есть аккаунт?
            <a href="${pageContext.request.contextPath}/login">Войти</a>
        </div>
    </form>
</div>

<script>
  document.getElementById('registerForm').addEventListener('submit', function(e) {
    const password = document.querySelector('input[name="password"]');
    const confirmPassword = document.querySelector('input[name="confirmPassword"]');

    if (password.value !== confirmPassword.value) {
      e.preventDefault();
      alert('Пароли не совпадают!');
      confirmPassword.focus();
    }
  });
</script>
</body>
</html>