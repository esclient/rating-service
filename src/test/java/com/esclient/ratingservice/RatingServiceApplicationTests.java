package com.esclient.ratingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;

@SpringBootTest(
    classes = RatingServiceApplication.class,
    exclude = GrpcServerAutoConfiguration.class  // ✅ Correct way to exclude in tests
)
@ActiveProfiles("test")
class RatingServiceApplicationTests {

    @Test
    void contextLoads() {}
}
