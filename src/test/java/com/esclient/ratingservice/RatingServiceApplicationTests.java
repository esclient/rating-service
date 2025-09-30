package com.esclient.ratingservice;

import com.esclient.ratingservice.config.SecretsConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RatingServiceApplicationTests {

  @MockBean private SecretsConfigService secretsConfigService;

  @Test
  void contextLoads() {
    // This test will pass if ApplicationContext loads correctly
  }
}
