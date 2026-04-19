FROM python:3.11-slim
WORKDIR /app
RUN groupadd -r executor && useradd -r -g executor executor
RUN chown executor:executor /app
USER executor