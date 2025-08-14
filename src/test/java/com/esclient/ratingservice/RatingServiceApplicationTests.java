package com.esclient.ratingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;

@SpringBootTest
@ImportAutoConfiguration(exclude = GrpcServerAutoConfiguration.class) // Exclude gRPC server auto-config
@ActiveProfiles("test")
class RatingServiceApplicationTests {

    @Test
    void contextLoads() {}
}
