<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Личный кабинет | AI Career Nav</title>
    <link href="https://googleapis.com" rel="stylesheet">
    <style>
      :root {
        --glass: rgba(255, 255, 255, 0.03);
        --border: rgba(255, 255, 255, 0.1);
        --accent: #007AFF;
        --success: #34c759;
        --error: #ff3b30;
      }

      * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }
      body { background: #000; color: white; min-height: 100vh; display: flex; align-items: center; justify-content: center; overflow: hidden; }

      /* Живой фон */
      .bg-glow {
        position: absolute; width: 600px; height: 600px;
        background: radial-gradient(circle, rgba(0, 122, 255, 0.1) 0%, transparent 70%);
        top: -10%; right: -10%; z-index: -1; filter: blur(60px);
      }

      .main-container {
        width: 1000px; height: 650px;
        background: var(--glass);
        backdrop-filter: blur(40px);
        border: 1px solid var(--border);
        border-radius: 40px;
        display: flex;
        box-shadow: 0 40px 100px rgba(0,0,0,0.8);
      }

      /* Sidebar */
      .sidebar {
        width: 280px; border-right: 1px solid var(--border);
        padding: 50px 40px; display: flex; flex-direction: column;
      }

      .user-avatar {
        width: 70px; height: 70px; background: linear-gradient(45deg, #007AFF, #5856d6);
        border-radius: 22px; display: flex; align-items: center; justify-content: center;
        font-size: 28px; font-weight: 600; margin-bottom: 25px;
      }

      .nav-item { color: rgba(255,255,255,0.4); margin: 15px 0; cursor: pointer; transition: 0.3s; font-size: 15px; text-decoration: none; }
      .nav-item.active { color: white; font-weight: 600; }
      .nav-item:hover { color: white; transform: translateX(5px); }

      /* Content Area */
      .content { flex: 1; padding: 60px; overflow-y: auto; }
      h1 { font-size: 38px; margin-bottom: 10px; font-weight: 600; }
      .status-text { color: rgba(255,255,255,0.4); margin-bottom: 40px; }

      /* Виджет загрузки */
      .upload-zone {
        background: rgba(255,255,255,0.03);
        border: 2px dashed var(--border);
        border-radius: 24px;
        padding: 40px;
        text-align: center;
        transition: 0.3s;
      }
      .upload-zone:hover { border-color: var(--accent); background: rgba(0,122,255,0.05); }

      input[type="file"] { display: none; }
      .file-label { cursor: pointer; color: var(--accent); font-weight: 500; }

      .btn-primary {
        background: #fff; color: #000; padding: 14px 28px;
        border-radius: 14px; border: none; font-weight: 600; cursor: pointer;
        margin-top: 20px; transition: 0.3s;
      }
      .btn-primary:hover { transform: scale(1.03); background: var(--accent); color: white; }

      /* Уведомления */
      .alert { padding: 15px; border-radius: 12px; margin-bottom: 20px; font-size: 14px; }
      .alert-success { background: rgba(52, 199, 89, 0.1); color: var(--success); border: 1px solid rgba(52, 199, 89, 0.2); }
      .alert-error { background: rgba(255, 59, 48, 0.1); color: var(--error); border: 1px solid rgba(255, 59, 48, 0.2); }

      .resume-badge {
        display: inline-flex; align-items: center; padding: 10px 16px;
        background: rgba(255,255,255,0.05); border-radius: 12px; margin-top: 20px;
      }
    </style>
</head>
<body>

<div class="bg-glow"></div>

<div class="main-container">
    <aside class="sidebar">
        <div class="user-avatar">
            ${sessionScope.userName != null ? sessionScope.userName.substring(0,1).toUpperCase() : 'U'}
        </div>
        <a class="nav-item active">Кабинет</a>
        <a class="nav-item">Анализ резюме</a>
        <a class="nav-item">Рекомендации</a>
        <a href="/logout" class="nav-item" style="margin-top: auto; color: var(--error)">Выйти</a>
    </aside>

    <main class="content">
        <h1>Привет, ${sessionScope.userName}!</h1>
        <p class="status-text">Регистрация: <fmt:formatDate value="${sessionScope.registrationDate}" pattern="dd.MM.yyyy"/></p>

        <!-- Вывод сообщений из сессии -->
        <c:if test="${not empty sessionScope.uploadSuccess}">
            <div class="alert alert-success">${sessionScope.uploadSuccess}</div>
            <% session.removeAttribute("uploadSuccess"); %>
        </c:if>
        <c:if test="${not empty sessionScope.uploadError}">
            <div class="alert alert-error">${sessionScope.uploadError}</div>
            <% session.removeAttribute("uploadError"); %>
        </c:if>

        <section>
            <form action="/personal-cabinet" method="POST" enctype="multipart/form-data">
                <div class="upload-zone">
                    <p style="margin-bottom: 10px;">Загрузите ваше резюме (PDF, DOCX)</p>
                    <label class="file-label">
                        Выберите файл
                        <input type="file" name="resumeFile" onchange="this.form.submit()">
                    </label>
                </div>
            </form>

            <c:if test="${sessionScope.resumeUploaded}">
                <div class="resume-badge">
                    <span style="margin-right: 10px;">📄</span>
                    <div>
                        <div style="font-size: 14px;">${sessionScope.resumeFilename}</div>
                        <div style="font-size: 11px; color: rgba(255,255,255,0.4)">
                            Загружено: <fmt:formatDate value="${sessionScope.resumeUploadDate}" pattern="dd.MM HH:mm"/>
                        </div>
                    </div>
                </div>
            </c:if>
        </section>
    </main>
</div>

</body>
</html>
