package org.example.aicareernav1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AiCareerNavApplicationTests {

  @Test
  void contextLoads(ApplicationContext context) {
    assertNotNull(context, "Spring context should not be null");
  }
}
