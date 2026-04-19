FROM php:8.2-cli-alpine
WORKDIR /app
# Создаем непривилегированного пользователя для безопасности
RUN addgroup -S executor && adduser -S executor -G executor
RUN chown executor:executor /app
USER executor