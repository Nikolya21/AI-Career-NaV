<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Вход - AI Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Login.css">
</head>
<body>
<div class="login-container">
    <h2>Вход</h2>

    <%-- Сообщение об успешной регистрации --%>
    <%
        String registered = request.getParameter("registered");
        String registeredEmail = request.getParameter("email");
        if ("true".equals(registered)) {
    %>
    <div class="success-message">
        <strong>✅ Регистрация успешна!</strong>
        <% if (registeredEmail != null && !registeredEmail.isEmpty()) { %>
        <p>Аккаунт для <strong><%= java.net.URLDecoder.decode(registeredEmail, "UTF-8") %></strong> создан.</p>
        <% } %>
        <p>Теперь вы можете войти в систему.</p>
    </div>
    <% } %>

    <%-- Сообщения об ошибках аутентификации --%>
    <%
        List<String> errors = (List<String>) request.getAttribute("errors");
        String prefilledEmail = (String) request.getAttribute("email");
        if (errors != null && !errors.isEmpty()) {
    %>
    <div class="error-message">
        <strong>Ошибка входа:</strong>
        <ul>
            <% for (String error : errors) { %>
            <li><%= error %></li>
            <% } %>
        </ul>
    </div>
    <% } %>

    <form action="${pageContext.request.contextPath}/login" method="post" class="login-form">
        <div class="input-group">
            <input type="email"
                   name="email"
                   value="<%= prefilledEmail != null ? prefilledEmail : (registeredEmail != null ? java.net.URLDecoder.decode(registeredEmail, "UTF-8") : "") %>"
                   placeholder="Электронная почта"
                   required
                   maxlength="255">
        </div>
        <div class="input-group">
            <input type="password"
                   name="password"
                   placeholder="Пароль"
                   required
                   minlength="6"
                   maxlength="100">
        </div>
        <button type="submit" class="btn-login">Войти</button>
        <div class="register-link">
            Нет аккаунта? <a href="http://localhost:8080/register">Зарегистрироваться</a>
        </div>
    </form>
</div>
</body>
</html>