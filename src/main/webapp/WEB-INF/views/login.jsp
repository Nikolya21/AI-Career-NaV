<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Future Authentication</title>
    <link href="https://googleapis.com" rel="stylesheet">
    <style>
      :root {
        --primary: #ffffff;
        --accent: #007AFF; /* Apple Blue */
        --glass: rgba(255, 255, 255, 0.08);
        --border: rgba(255, 255, 255, 0.15);
      }

      * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }

      body {
        height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #000;
        overflow: hidden;
        perspective: 1000px;
      }

      /* Интерактивный живой фон */
      .gradient-bg {
        position: fixed;
        top: 0; left: 0; width: 100%; height: 100%;
        background: radial-gradient(circle at 50% 50%, #1a1a1a 0%, #000 100%);
        z-index: -1;
      }

      .blob {
        position: absolute;
        width: 600px; height: 600px;
        background: linear-gradient(135deg, #6366f1 0%, #a855f7 50%, #ec4899 100%);
        filter: blur(80px);
        border-radius: 50%;
        opacity: 0.4;
        animation: float 20s infinite alternate;
      }

      @keyframes float {
        0% { transform: translate(-10%, -10%) rotate(0deg); }
        100% { transform: translate(20%, 20%) rotate(360deg); }
      }

      /* Карточка с эффектом стекла */
      .login-card {
        background: var(--glass);
        backdrop-filter: blur(25px) saturate(180%);
        -webkit-backdrop-filter: blur(25px) saturate(180%);
        border: 1px solid var(--border);
        border-radius: 32px;
        padding: 60px 45px;
        width: 420px;
        text-align: center;
        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        transition: transform 0.4s ease;
      }

      .login-card:hover {
        transform: translateY(-5px) scale(1.01);
        border-color: rgba(255, 255, 255, 0.3);
      }

      h1 {
        color: var(--primary);
        font-size: 32px;
        font-weight: 700;
        margin-bottom: 8px;
        letter-spacing: -1px;
      }

      p.desc {
        color: rgba(255, 255, 255, 0.5);
        font-size: 15px;
        margin-bottom: 40px;
      }

      .input-box {
        position: relative;
        margin-bottom: 20px;
      }

      input {
        width: 100%;
        padding: 16px 20px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid var(--border);
        border-radius: 14px;
        outline: none;
        color: #fff;
        font-size: 16px;
        transition: 0.3s;
      }

      input:focus {
        background: rgba(255, 255, 255, 0.1);
        border-color: var(--accent);
        box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.15);
      }

      button {
        width: 100%;
        padding: 16px;
        background: #fff;
        color: #000;
        border: none;
        border-radius: 14px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
        margin-top: 10px;
      }

      button:hover {
        background: var(--accent);
        color: #fff;
        transform: scale(1.02);
      }

      button:active { transform: scale(0.98); }

      .footer {
        margin-top: 30px;
        font-size: 13px;
        color: rgba(255, 255, 255, 0.4);
      }

      .footer a { color: var(--primary); text-decoration: none; opacity: 0.8; }
      .footer a:hover { opacity: 1; text-decoration: underline; }
    </style>
</head>
<body>

<div class="gradient-bg"></div>
<div class="blob"></div>

<div class="login-card">
    <h1>Welcome back</h1>
    <p class="desc">Enter your credentials to access the portal</p>

    <form action="${pageContext.request.contextPath}/login" method="POST">
        <div class="input-box">
            <input type="text" name="email" placeholder="Email" required>
        </div>
        <div class="input-box">
            <input type="password" name="password" placeholder="Password" required>
        </div>
        <button type="submit">Continue</button>
    </form>

    <c:if test="${not empty errors}">
        <div style="color: red;">
            <c:forEach items="${errors}" var="error">${error}<br/></c:forEach>
        </div>
    </c:if>

    <div class="footer">
        Don't have an account? <a href="#">Request access</a>
    </div>
</div>

<script>
  // Интерактивное движение "светового пятна" за мышью
  const blob = document.querySelector('.blob');
  document.body.addEventListener('mousemove', (e) => {
    const x = e.clientX;
    const y = e.clientY;
    blob.style.left = x - 300 + 'px';
    blob.style.top = y - 300 + 'px';
  });
</script>
</body>
</html>
