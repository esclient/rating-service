package com.esclient.ratingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;

@SpringBootTest
@ActiveProfiles("test")
class RatingServiceApplicationTests {

    @TestConfiguration
    @ImportAutoConfiguration(exclude = GrpcServerAutoConfiguration.class)
    static class TestConfig {
        // Empty class — just disables gRPC auto-config for this test
    }

    @Test
    void contextLoads() {}
}
