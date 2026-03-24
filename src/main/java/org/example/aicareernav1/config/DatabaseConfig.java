package org.example.aicareernav1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DatabaseConfig {

  private final DataSource dataSource;
  private final ServiceDataGenerator serviceDataGenerator;

  @Autowired
  public DatabaseConfig(DataSource dataSource, ServiceDataGenerator serviceDataGenerator) {
    this.dataSource = dataSource;
    this.serviceDataGenerator = serviceDataGenerator;
  }

  @PostConstruct
  public void initDatabaseSchema() {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("CREATE SCHEMA IF NOT EXISTS aicareer");
      // здесь можно выполнить SQL-скрипты, если нужно
    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialize database schema", e);
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void generateTestData() {
    serviceDataGenerator.generateAllTestData();
  }
}