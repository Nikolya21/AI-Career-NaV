FROM gcc:latest
WORKDIR /app
RUN groupadd -r executor && useradd -r -g executor executor
RUN chown executor:executor /app
USER executor