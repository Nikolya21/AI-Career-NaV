<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Регистрация</title>
</head>
<body>
<h2>Регистрация</h2>
<c:if test="${not empty errors}">
    <ul style="color: red;">
        <c:forEach items="${errors}" var="error">
            <li>${error}</li>
        </c:forEach>
    </ul>
</c:if>
<form action="/register" method="post">
    <label>Имя:</label>
    <input type="text" name="name" value="${userRegistrationDto.name}" required><br>

    <label>Email:</label>
    <input type="email" name="email" value="${userRegistrationDto.email}" required><br>

    <label>Пароль:</label>
    <input type="password" name="password" required><br>

    <label>Подтверждение пароля:</label>
    <input type="password" name="confirmPassword" required><br>

    <input type="submit" value="Зарегистрироваться">
</form>
<p>Уже есть аккаунт? <a href="/login">Войти</a></p>
</body>
</html>