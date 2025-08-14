package com.esclient.ratingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // picks up application-test.yml
class RatingServiceApplicationTests {

    @Test
    void contextLoads() {}
}
