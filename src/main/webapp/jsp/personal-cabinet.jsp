<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.example.aicareernav1.model.user.User" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Личный кабинет - МТС</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/personal-cabinet.css">
    <style>
      /* Основные стили страницы */
      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
        background: linear-gradient(135deg, #a4a4a5 0%, #e4bfc0 100%);
        min-height: 100vh;
      }

      .container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 20px;
      }

      /* Шапка в стиле МТС */
      .mts-header {
        background: linear-gradient(135deg, #e30613, #ff0000);
        color: white;
        border-radius: 15px 15px 0 0;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        margin-bottom: 30px;
      }

      .header-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 30px;
      }

      .logo {
        display: flex;
        align-items: center;
        gap: 15px;
      }

      .mts-logo {
        font-size: 32px;
        font-weight: 900;
        background: white;
        color: #e30613;
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
      }

      .logo-text {
        font-size: 24px;
        font-weight: 600;
      }

      .header-nav {
        display: flex;
        gap: 25px;
      }

      .nav-link {
        color: white;
        text-decoration: none;
        font-size: 16px;
        font-weight: 500;
        padding: 8px 16px;
        border-radius: 6px;
        transition: all 0.3s ease;
      }

      .nav-link:hover {
        background: rgba(255, 255, 255, 0.15);
      }

      /* Основной контент */
      .main-content {
        display: grid;
        grid-template-columns: 1fr;
        gap: 30px;
      }

      @media (min-width: 768px) {
        .main-content {
          grid-template-columns: 2fr 1fr;
        }
      }

      .cabinet-card {
        background: white;
        border-radius: 15px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        overflow: hidden;
      }

      /* Блок аватара */
      .avatar-section {
        background: linear-gradient(135deg, #2c3e50, #34495e);
        padding: 40px 30px;
        text-align: center;
      }

      .avatar-container {
        display: inline-flex;
        flex-direction: column;
        align-items: center;
        gap: 20px;
      }

      .avatar {
        width: 120px;
        height: 120px;
        border-radius: 50%;
        background: linear-gradient(135deg, #3498db, #2980b9);
        display: flex;
        align-items: center;
        justify-content: center;
        border: 5px solid white;
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
      }

      .avatar-initials {
        font-size: 48px;
        font-weight: 700;
        color: white;
      }

      .change-avatar-btn {
        background: rgba(255, 255, 255, 0.1);
        color: white;
        border: 2px solid white;
        padding: 10px 24px;
        border-radius: 25px;
        cursor: pointer;
        font-size: 14px;
        font-weight: 600;
        transition: all 0.3s ease;
        backdrop-filter: blur(10px);
      }

      .change-avatar-btn:hover {
        background: white;
        color: #2c3e50;
      }

      /* Информация о пользователе */
      .user-info-section {
        padding: 40px 30px;
        border-bottom: 1px solid #eee;
      }

      .user-name {
        font-size: 32px;
        color: #2c3e50;
        margin-bottom: 30px;
        text-align: center;
      }

      .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 25px;
      }

      .info-item {
        padding: 20px;
        background: #f8f9fa;
        border-radius: 10px;
        border-left: 4px solid #3498db;
      }

      .info-label {
        display: block;
        color: #7f8c8d;
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 8px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .info-value {
        font-size: 18px;
        color: #2c3e50;
        font-weight: 600;
      }

      /* Уведомления */
      .notification {
        padding: 15px 20px;
        border-radius: 10px;
        margin: 15px 30px;
        display: flex;
        align-items: center;
        gap: 12px;
        animation: slideIn 0.3s ease-out;
      }

      .notification-success {
        background: linear-gradient(135deg, #d4edda, #c3e6cb);
        border: 2px solid #28a745;
        color: #155724;
      }

      .notification-error {
        background: linear-gradient(135deg, #f8d7da, #f5c6cb);
        border: 2px solid #dc3545;
        color: #721c24;
      }

      .notification-icon {
        font-size: 20px;
      }

      @keyframes slideIn {
        from {
          opacity: 0;
          transform: translateY(-10px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      /* Раздел с резюме */
      .resume-section {
        padding: 30px;
        background: #f8f9fa;
        border-top: 1px solid #eee;
      }

      .resume-upload h3 {
        font-size: 24px;
        color: #2c3e50;
        margin-bottom: 25px;
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .resume-info {
        margin-bottom: 25px;
      }

      .resume-status {
        display: flex;
        align-items: center;
        padding: 20px;
        border-radius: 12px;
        gap: 20px;
        margin-bottom: 15px;
      }

      .resume-status.uploaded {
        background: linear-gradient(135deg, #d4edda, #c3e6cb);
        border: 2px solid #28a745;
      }

      .resume-status.not-uploaded {
        background: linear-gradient(135deg, #f8d7da, #f5c6cb);
        border: 2px solid #dc3545;
      }

      .resume-icon {
        font-size: 28px;
        width: 50px;
        height: 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: white;
        border-radius: 50%;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }

      .resume-details {
        flex: 1;
      }

      .resume-details strong {
        display: block;
        color: #155724;
        font-size: 18px;
        margin-bottom: 5px;
      }

      .resume-details .filename {
        display: block;
        color: #0f5132;
        font-weight: 500;
        margin: 8px 0;
        font-size: 16px;
        word-break: break-all;
      }

      .resume-details .upload-date {
        display: block;
        font-size: 14px;
        color: #6c757d;
      }

      .resume-details .resume-hint {
        display: block;
        color: #721c24;
        font-size: 15px;
        margin-top: 8px;
      }

      .resume-actions {
        display: flex;
        gap: 15px;
        flex-wrap: wrap;
        margin-bottom: 20px;
      }

      .btn-resume-upload,
      .btn-resume-change,
      .btn-resume-view,
      .btn-roadmap {
        padding: 14px 28px;
        border: none;
        border-radius: 10px;
        cursor: pointer;
        font-size: 16px;
        font-weight: 600;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        text-decoration: none;
        min-width: 200px;
      }

      .btn-resume-upload {
        background: linear-gradient(135deg, #3498db, #2980b9);
        color: white;
        box-shadow: 0 6px 15px rgba(52, 152, 219, 0.3);
      }

      .btn-resume-upload:hover {
        background: linear-gradient(135deg, #2980b9, #2471a3);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(52, 152, 219, 0.4);
      }

      .btn-resume-change {
        background: linear-gradient(135deg, #f39c12, #e67e22);
        color: white;
        box-shadow: 0 6px 15px rgba(243, 156, 18, 0.3);
      }

      .btn-resume-change:hover {
        background: linear-gradient(135deg, #e67e22, #d35400);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(243, 156, 18, 0.4);
      }

      .btn-resume-view {
        background: linear-gradient(135deg, #2ecc71, #27ae60);
        color: white;
        box-shadow: 0 6px 15px rgba(46, 204, 113, 0.3);
      }

      .btn-resume-view:hover {
        background: linear-gradient(135deg, #27ae60, #219653);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(46, 204, 113, 0.4);
      }

      .btn-roadmap {
        background: linear-gradient(135deg, #28a745, #20c997);
        color: white;
        border: none;
        box-shadow: 0 6px 15px rgba(40, 167, 69, 0.3);
      }

      .btn-roadmap:hover {
        background: linear-gradient(135deg, #218838, #1e9e8a);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(40, 167, 69, 0.4);
      }

      .btn-roadmap.disabled {
        background: linear-gradient(135deg, #95a5a6, #7f8c8d);
        cursor: not-allowed;
        opacity: 0.7;
        box-shadow: none;
      }

      .btn-roadmap.disabled:hover {
        transform: none;
        box-shadow: none;
      }

      .resume-formats {
        text-align: center;
        color: #6c757d;
        font-size: 13px;
        margin-top: 15px;
        padding-top: 15px;
        border-top: 1px solid #dee2e6;
      }

      /* Форма загрузки резюме */
      .upload-form-container {
        background: white;
        border: 2px dashed #3498db;
        border-radius: 12px;
        padding: 25px;
        margin: 15px 0;
        text-align: center;
        display: none; /* Скрыта по умолчанию, показывается при нажатии на кнопку */
      }

      .upload-form-container.active {
        display: block;
        animation: fadeIn 0.3s ease-out;
      }

      .upload-form {
        display: flex;
        flex-direction: column;
        gap: 15px;
        align-items: center;
      }

      .file-input-wrapper {
        position: relative;
        width: 100%;
        max-width: 400px;
      }

      .file-input-label {
        display: block;
        background: linear-gradient(135deg, #f8f9fa, #e9ecef);
        border: 2px solid #3498db;
        border-radius: 8px;
        padding: 25px 20px;
        cursor: pointer;
        transition: all 0.3s ease;
        text-align: center;
      }

      .file-input-label:hover {
        background: linear-gradient(135deg, #e9ecef, #dee2e6);
        border-color: #2980b9;
      }

      .file-input-label.dragover {
        background: linear-gradient(135deg, #e3f2fd, #bbdefb);
        border-color: #1a73e8;
      }

      .upload-icon {
        font-size: 40px;
        color: #3498db;
        margin-bottom: 10px;
        display: block;
      }

      .file-input {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        cursor: pointer;
      }

      .file-info {
        margin-top: 10px;
        padding: 10px;
        background: #f8f9fa;
        border-radius: 6px;
        font-size: 14px;
        color: #495057;
      }

      .form-actions {
        display: flex;
        gap: 10px;
        justify-content: center;
        width: 100%;
        margin-top: 15px;
      }

      .btn-submit {
        background: linear-gradient(135deg, #28a745, #20c997);
        color: white;
        border: none;
        padding: 12px 25px;
        border-radius: 8px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
      }

      .btn-submit:hover {
        background: linear-gradient(135deg, #218838, #1e9e8a);
        transform: translateY(-2px);
      }

      .btn-cancel {
        background: linear-gradient(135deg, #6c757d, #5a6268);
        color: white;
        border: none;
        padding: 12px 25px;
        border-radius: 8px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
      }

      .btn-cancel:hover {
        background: linear-gradient(135deg, #5a6268, #495057);
        transform: translateY(-2px);
      }

      /* Кнопки действий */
      .actions-section {
        padding: 30px;
        display: flex;
        gap: 20px;
        flex-wrap: wrap;
        justify-content: center;
        background: white;
        border-top: 1px solid #eee;
      }

      .btn {
        padding: 15px 32px;
        border-radius: 10px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        border: none;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        min-width: 200px;
      }

      .btn-primary {
        background: linear-gradient(135deg, #e30613, #ff0000);
        color: white;
        box-shadow: 0 6px 15px rgba(227, 6, 19, 0.3);
      }

      .btn-primary:hover {
        background: linear-gradient(135deg, #c00000, #a00000);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(227, 6, 19, 0.4);
      }

      .btn-secondary {
        background: linear-gradient(135deg, #3498db, #2980b9);
        color: white;
        box-shadow: 0 6px 15px rgba(52, 152, 219, 0.3);
      }

      .btn-secondary:hover {
        background: linear-gradient(135deg, #2980b9, #2471a3);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(52, 152, 219, 0.4);
      }

      .btn-logout {
        background: linear-gradient(135deg, #e74c3c, #c0392b);
        color: white;
        box-shadow: 0 6px 15px rgba(231, 76, 60, 0.3);
      }

      .btn-logout:hover {
        background: linear-gradient(135deg, #c0392b, #a93226);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(231, 76, 60, 0.4);
      }

      /* Дополнительные карточки */
      .additional-cards {
        display: flex;
        flex-direction: column;
        gap: 30px;
      }

      .service-card,
      .promo-card {
        background: white;
        border-radius: 15px;
        padding: 30px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
      }

      .service-card h3,
      .promo-card h3 {
        font-size: 22px;
        color: #2c3e50;
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 2px solid #eee;
      }

      .services-list {
        list-style: none;
      }

      .services-list li {
        padding: 12px 0;
        border-bottom: 1px solid #f8f9fa;
        color: #495057;
        font-size: 16px;
      }

      .services-list li:last-child {
        border-bottom: none;
      }

      .promo-card p {
        color: #6c757d;
        line-height: 1.6;
        margin-bottom: 25px;
        font-size: 16px;
      }

      .btn-promo {
        background: linear-gradient(135deg, #ab94e4, #e1a3c3);
        color: white;
        border: none;
        padding: 14px 28px;
        border-radius: 10px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        width: 100%;
        transition: all 0.3s ease;
      }

      .btn-promo:hover {
        background: linear-gradient(135deg, #8e44ad, #7d3c98);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(155, 89, 182, 0.4);
      }

      /* Форма загрузки PNG в шапке */
      .upload-form {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-right: 20px;
      }

      .upload-form input[type="file"] {
        font-size: 14px;
        padding: 4px 8px;
        border-radius: 6px;
        border: 1px solid #ccc;
      }

      .upload-png-btn {
        background: white;
        color: #e30613;
        border: 2px solid #e30613;
        padding: 6px 14px;
        border-radius: 20px;
        font-weight: 600;
        cursor: pointer;
        font-size: 14px;
        transition: all 0.2s;
      }

      .upload-png-btn:hover {
        background: #e30613;
        color: white;
      }

      .upload-error {
        color: #dc3545;
        font-size: 12px;
        white-space: nowrap;
      }

      /* Анимации */
      @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
      }

      .cabinet-card,
      .service-card,
      .promo-card {
        animation: fadeIn 0.6s ease-out;
      }

      /* Адаптивность */
      @media (max-width: 768px) {
        .header-content {
          flex-direction: column;
          gap: 20px;
          text-align: center;
        }

        .header-nav {
          flex-wrap: wrap;
          justify-content: center;
        }

        .info-grid {
          grid-template-columns: 1fr;
        }

        .resume-actions,
        .actions-section,
        .form-actions {
          flex-direction: column;
        }

        .btn,
        .btn-resume-upload,
        .btn-resume-change,
        .btn-resume-view,
        .btn-roadmap,
        .btn-submit,
        .btn-cancel {
          width: 100%;
          min-width: auto;
        }

        .file-input-wrapper {
          max-width: 100%;
        }
      }
    </style>
</head>
<body>
<div class="container">
    <!-- Шапка в стиле МТС -->
    <header class="mts-header">
        <div class="header-content">
            <div class="logo">
                <span class="mts-logo">МТС</span>
                <span class="logo-text">Личный кабинет</span>
            </div>
            <nav class="header-nav">
                <button class="btn-primary" onclick="location.href='${pageContext.request.contextPath}/choose-vacancy'">
                    🎯 Выбрать вакансию
                </button>
                <a href="#" class="nav-link">Услуги</a>
                <a href="#" class="nav-link">Помощь</a>
            </nav>
        </div>
    </header>

    <!-- Основной контент -->
    <main class="main-content">
        <div class="cabinet-card">
            <!-- Блок аватара -->
            <div class="avatar-section">
                <div class="avatar-container">
                    <div class="avatar">
                        <%
                            String userEmail = (String) session.getAttribute("userEmail");
                            String userName = (String) session.getAttribute("userName");
                            String initials = "П";
                            if (userName != null && !userName.isEmpty()) {
                                initials = userName.substring(0, 1).toUpperCase();
                            } else if (userEmail != null && !userEmail.isEmpty()) {
                                initials = userEmail.substring(0, 1).toUpperCase();
                            }
                        %>
                        <span class="avatar-initials"><%= initials %></span>
                    </div>
                    <button class="change-avatar-btn">Изменить фото</button>
                </div>
            </div>

            <!-- Информация о пользователе -->
            <div class="user-info-section">
                <h1 class="user-name">
                    <%
                        if (userName != null && !userName.isEmpty()) {
                            out.print(userName);
                        } else {
                            out.print("Пользователь");
                        }
                    %>
                </h1>

                <div class="info-grid">
                    <div class="info-item">
                        <label class="info-label">Электронная почта</label>
                        <div class="info-value">
                            <%= userEmail != null ? userEmail : "Не указано" %>
                        </div>
                    </div>

                    <div class="info-item">
                        <label class="info-label">ID пользователя</label>
                        <div class="info-value">
                            <%
                                Long userId = (Long) session.getAttribute("userId");
                                out.print(userId != null ? userId : "Не указано");
                            %>
                        </div>
                    </div>

                    <div class="info-item">
                        <label class="info-label">Статус</label>
                        <div class="info-value">Активный</div>
                    </div>

                    <div class="info-item">
                        <label class="info-label">Дата регистрации</label>
                        <div class="info-value">
                            <%
                                java.util.Date registrationDate = (java.util.Date) session.getAttribute("registrationDate");
                                if (registrationDate != null) {
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                                    out.print(sdf.format(registrationDate));
                                } else {
                                    out.print("Сегодня");
                                }
                            %>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Уведомления -->
            <%
                String uploadSuccess = (String) session.getAttribute("uploadSuccess");
                String uploadError = (String) session.getAttribute("uploadError");

                if (uploadSuccess != null) {
            %>
            <div class="notification notification-success">
                <span class="notification-icon">✅</span>
                <span><%= uploadSuccess %></span>
            </div>
            <%
                    // Удаляем сообщение после показа
                    session.removeAttribute("uploadSuccess");
                }

                if (uploadError != null) {
            %>
            <div class="notification notification-error">
                <span class="notification-icon">❌</span>
                <span><%= uploadError %></span>
            </div>
            <%
                    // Удаляем сообщение после показа
                    session.removeAttribute("uploadError");
                }
            %>

            <!-- Раздел для загрузки резюме -->
            <div class="resume-section">
                <div class="resume-upload">
                    <h3>📄 Резюме</h3>
                    <div class="resume-info">
                        <%
                            String resumeFilename = (String) session.getAttribute("resumeFilename");
                            Boolean resumeUploaded = (Boolean) session.getAttribute("resumeUploaded");

                            if (resumeUploaded != null && resumeUploaded && resumeFilename != null) {
                        %>
                        <div class="resume-status uploaded">
                            <span class="resume-icon">✅</span>
                            <div class="resume-details">
                                <strong>Резюме загружено</strong>
                                <span class="filename"><%= resumeFilename %></span>
                                <span class="upload-date">
                                    Загружено:
                                    <%
                                        java.util.Date resumeUploadDate = (java.util.Date) session.getAttribute("resumeUploadDate");
                                        if (resumeUploadDate != null) {
                                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
                                            out.print(sdf.format(resumeUploadDate));
                                        } else {
                                            out.print("Только что");
                                        }
                                    %>
                                </span>
                            </div>
                            <button class="btn-resume-change" onclick="showUploadForm()">Заменить</button>
                            <button class="btn-resume-view" onclick="viewResume()">Просмотреть</button>
                        </div>
                        <% } else { %>
                        <div class="resume-status not-uploaded">
                            <span class="resume-icon">📄</span>
                            <div class="resume-details">
                                <strong>Резюме не загружено</strong>
                                <span class="resume-hint">Загрузите ваше резюме для лучшего подбора вакансий</span>
                            </div>
                        </div>
                        <% } %>
                    </div>

                    <!-- Форма загрузки резюме (скрыта по умолчанию) -->
                    <div id="uploadFormContainer" class="upload-form-container">
                        <form id="resumeUploadForm" class="upload-form" method="POST"
                              action="${pageContext.request.contextPath}/personal-cabinet"
                              enctype="multipart/form-data">
                            <div class="file-input-wrapper">
                                <label for="resumeFile" class="file-input-label" id="fileInputLabel">
                                    <span class="upload-icon">📎</span>
                                    <span>Нажмите или перетащите файл для загрузки</span>
                                    <input type="file" id="resumeFile" name="resumeFile"
                                           class="file-input" accept=".pdf,.doc,.docx,.txt"
                                           onchange="updateFileInfo(this)">
                                </label>
                                <div id="fileInfo" class="file-info" style="display: none;">
                                    Выбран файл: <span id="fileName"></span> (<span id="fileSize"></span>)
                                </div>
                            </div>

                            <div class="form-actions">
                                <button type="submit" class="btn-submit">📤 Загрузить резюме</button>
                                <button type="button" class="btn-cancel" onclick="hideUploadForm()">Отмена</button>
                            </div>
                        </form>
                    </div>

                    <div class="resume-actions">
                        <% if (resumeUploaded == null || !resumeUploaded) { %>
                        <button class="btn-resume-upload" onclick="showUploadForm()">
                            📎 Прикрепить резюме
                        </button>
                        <% } else { %>
                        <button class="btn-resume-change" onclick="showUploadForm()">
                            🔄 Заменить резюме
                        </button>
                        <% } %>

                        <%
                            Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
                            Object generatedRoadmap = session.getAttribute("generatedRoadmap");
                            String contextPath = request.getContextPath();
                            String roadmapUrl = contextPath + "/career-roadmap";
                            boolean isRoadmapAvailable = (discussionCompleted != null && discussionCompleted && generatedRoadmap != null);
                        %>
                        <button class="btn-roadmap <%= isRoadmapAvailable ? "" : "disabled" %>"
                                onclick="<%= isRoadmapAvailable ? "window.location.href='" + roadmapUrl + "'" : "alert('Сначала завершите обсуждение вакансии, чтобы получить персональный план')" %>"
                                title="<%= isRoadmapAvailable ? "Перейти к карьерному плану" : "Сначала завершите обсуждение вакансии" %>">
                            🗺️ Мой карьерный план
                        </button>
                    </div>

                    <div class="resume-formats">
                        <small>Поддерживаемые форматы: PDF, DOC, DOCX, TXT (максимум 10 МБ)</small>
                    </div>
                </div>
            </div>

            <!-- Кнопки действий -->
            <div class="actions-section">
                <button class="btn-primary" onclick="location.href='${pageContext.request.contextPath}/choose-vacancy'">
                    🎯 Выбрать вакансию
                </button>
                <button class="btn btn-secondary" onclick="history.back()">
                    ↩️ Вернуться назад
                </button>
                <button class="btn btn-logout" onclick="logout()">
                    🚪 Выйти из аккаунта
                </button>
            </div>
        </div>

        <!-- Дополнительные карточки -->
        <div class="additional-cards">
            <div class="service-card">
                <h3>Активность</h3>
                <ul class="services-list">
                    <li>Сообщений отправлено:
                        <%
                            java.util.List<String> messageHistory = (java.util.List<String>) session.getAttribute("messageHistory");
                            int messageCount = messageHistory != null ? messageHistory.size() / 2 : 0;
                            out.print(messageCount);
                        %>
                    </li>
                    <li>Последняя активность: Сегодня</li>
                    <li>Статус: Online</li>
                </ul>
            </div>

            <div class="promo-card">
                <h3>Специальные предложения</h3>
                <p>Получите персональную консультацию по карьерному развитию</p>
                <button class="btn-primary" onclick="location.href='${pageContext.request.contextPath}/choose-vacancy'">
                    🎯 Выбрать вакансию
                </button>
            </div>
        </div>
    </main>
</div>

<script>
  function logout() {
    if (confirm('Вы уверены, что хотите выйти из аккаунта?')) {
      window.location.href = '<%= request.getContextPath() %>/logout';
    }
  }

  // Показать форму загрузки
  function showUploadForm() {
    const formContainer = document.getElementById('uploadFormContainer');
    formContainer.classList.add('active');

    // Прокручиваем к форме
    formContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  // Скрыть форму загрузки
  function hideUploadForm() {
    const formContainer = document.getElementById('uploadFormContainer');
    formContainer.classList.remove('active');

    // Сбрасываем форму
    document.getElementById('resumeUploadForm').reset();
    document.getElementById('fileInfo').style.display = 'none';
  }

  // Обновить информацию о выбранном файле
  function updateFileInfo(input) {
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');
    const fileInputLabel = document.getElementById('fileInputLabel');

    if (input.files && input.files[0]) {
      const file = input.files[0];
      const fileSizeKB = Math.round(file.size / 1024);
      const fileSizeMB = fileSizeKB > 1024 ? (fileSizeKB / 1024).toFixed(1) + ' MB' : fileSizeKB + ' KB';

      fileName.textContent = file.name;
      fileSize.textContent = fileSizeMB;
      fileInfo.style.display = 'block';

      // Добавляем класс для визуальной обратной связи
      fileInputLabel.classList.add('dragover');
      setTimeout(() => fileInputLabel.classList.remove('dragover'), 300);
    } else {
      fileInfo.style.display = 'none';
    }
  }

  // Обработка drag and drop
  document.addEventListener('DOMContentLoaded', function() {
    const fileInputLabel = document.getElementById('fileInputLabel');
    const fileInput = document.getElementById('resumeFile');

    if (fileInputLabel && fileInput) {
      // Обработка перетаскивания файлов
      ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        fileInputLabel.addEventListener(eventName, preventDefaults, false);
      });

      function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
      }

      ['dragenter', 'dragover'].forEach(eventName => {
        fileInputLabel.addEventListener(eventName, highlight, false);
      });

      ['dragleave', 'drop'].forEach(eventName => {
        fileInputLabel.addEventListener(eventName, unhighlight, false);
      });

      function highlight() {
        fileInputLabel.classList.add('dragover');
      }

      function unhighlight() {
        fileInputLabel.classList.remove('dragover');
      }

      fileInputLabel.addEventListener('drop', handleDrop, false);

      function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        fileInput.files = files;
        updateFileInfo(fileInput);
      }
    }

    // Валидация формы перед отправкой
    const form = document.getElementById('resumeUploadForm');
    if (form) {
      form.addEventListener('submit', function(e) {
        const fileInput = document.getElementById('resumeFile');
        if (!fileInput.files || fileInput.files.length === 0) {
          e.preventDefault();
          alert('Пожалуйста, выберите файл для загрузки');
          return false;
        }

        const file = fileInput.files[0];
        const maxSize = 10 * 1024 * 1024; // 10 MB

        if (file.size > maxSize) {
          e.preventDefault();
          alert('Файл слишком большой! Максимальный размер: 10 МБ');
          return false;
        }

        // Показываем индикатор загрузки
        showUploadProgress();
      });
    }
  });

  // Показать прогресс загрузки
  function showUploadProgress() {
    const modal = document.createElement('div');
    modal.innerHTML = `
      <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;">
        <div style="background: white; padding: 30px; border-radius: 10px; text-align: center; min-width: 300px;">
          <div class="spinner" style="border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 2s linear infinite; margin: 0 auto 15px;"></div>
          <h3 style="margin-bottom: 10px;">Загрузка резюме...</h3>
          <p style="color: #666;">Пожалуйста, подождите</p>
          <style>@keyframes spin {0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); }}</style>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
  }

  // Просмотр резюме
  function viewResume() {
    const resumeFilename = '<%= resumeFilename != null ? resumeFilename : "" %>';
    if (resumeFilename) {
      alert('Просмотр резюме: ' + resumeFilename + '\n\nВ реальном приложении здесь будет открытие файла');
      // window.open('<%= request.getContextPath() %>/download-resume?filename=' + encodeURIComponent(resumeFilename), '_blank');
    }
  }

  // Автоматически закрываем уведомления через 5 секунд
  document.addEventListener('DOMContentLoaded', function() {
    const notifications = document.querySelectorAll('.notification');
    notifications.forEach(notification => {
      setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateY(-10px)';
        setTimeout(() => {
          if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
          }
        }, 300);
      }, 5000);
    });
  });
</script>
</body>
</html>ё