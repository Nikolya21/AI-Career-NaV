# Используем образ под твой процессор (Ubuntu-based)
FROM eclipse-temurin:17-jdk

# Создаем рабочую директорию
WORKDIR /app

# Создаем группу и пользователя без пароля (синтаксис для Ubuntu)
RUN groupadd -r executor && useradd -r -g executor executor

# Настраиваем права на папку, чтобы наш пользователь мог в ней писать
RUN chown executor:executor /app

# Переключаемся на этого пользователя
USER executor

# Команда по умолчанию (не обязательна, так как мы запускаем через Java)
CMD ["sh"]