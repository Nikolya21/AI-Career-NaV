package com.aicareer.core.config;

public class GigaChatConfig {
  private final String clientId;
  private final String clientSecret;
  private final String scope;
  private final int timeoutSeconds;

  public GigaChatConfig() {
    this.clientId = System.getenv("GIGACHAT_CLIENT_ID");
    this.clientSecret = System.getenv("GIGACHAT_CLIENT_SECRET");
    this.scope = System.getenv("GIGACHAT_SCOPE");
    this.timeoutSeconds = Integer.parseInt(
        System.getenv().getOrDefault("GIGACHAT_TIMEOUT_SECONDS", "30") // дефолт 30 сек
    );
  }

  public GigaChatConfig(String clientId, String clientSecret, String scope, int timeoutSeconds) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.scope = scope;
    this.timeoutSeconds = timeoutSeconds;
  }

  // Геттеры
  public String getClientId() { return clientId; }
  public String getClientSecret() { return clientSecret; }
  public String getScope() { return scope; }
  public int getTimeoutSeconds() { return timeoutSeconds; } // ← НОВЫЙ ГЕТТЕР
  public int getTimeoutMillis() { return timeoutSeconds * 1000; } // удобно для Java
}