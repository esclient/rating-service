package com.esclient.ratingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Picks up application-test.yml
class RatingServiceApplicationTests {

  @Test
  void contextLoads() {
    // This test will pass if ApplicationContext loads correctly
  }
}
