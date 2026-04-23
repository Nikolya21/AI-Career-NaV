FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# --- Вторая стадия: Рантайм ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 1. Переключаемся на root, чтобы установить пакеты
USER root

# 2. Устанавливаем Docker-клиент (для Ubuntu/Jammy)
RUN apt-get update && \
    apt-get install -y docker.io && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

# На всякий случай убедимся, что порт совпадает с твоим application.properties (там 8086)
EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]