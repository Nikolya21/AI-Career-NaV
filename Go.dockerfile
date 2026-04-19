FROM golang:1.21-alpine
WORKDIR /app
RUN addgroup -S executor && adduser -S executor -G executor
RUN chown executor:executor /app
USER executor