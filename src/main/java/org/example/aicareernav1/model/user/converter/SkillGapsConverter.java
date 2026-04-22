package org.example.aicareernav1.model.user.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Converter
public class SkillGapsConverter implements AttributeConverter<Map<String, Double>, String> {

  public static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, Double> attribute) {
    if (attribute == null) return null;
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert map to JSON", e);
    }
  }

  @Override
  public Map<String, Double> convertToEntityAttribute(String dbData) {
    if (dbData == null) return Collections.emptyMap();
    try {
      return objectMapper.readValue(dbData, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert JSON to map", e);
    }
  }
}