package org.example.aicareernav1.enums;

public enum CheckpointStatus {
  LOCKED,      // Еще не доступен
  ACTIVE,      // Текущий в процессе изучения
  RETRY,       // Назначена работа над ошибками
  COMPLETED,    // Успешно пройден
  GENERATING,  // Прямо сейчас ИИ пишет контент (ЛОАДЕР ТУТ!)
  ERROR        // Произошла ошибка генерации
}