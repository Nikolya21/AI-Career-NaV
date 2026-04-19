FROM mcr.microsoft.com/dotnet/sdk:7.0
WORKDIR /app
RUN groupadd -r executor && useradd -r -g executor executor
RUN chown executor:executor /app
USER executor