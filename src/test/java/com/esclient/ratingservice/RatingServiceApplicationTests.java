package com.esclient.ratingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(exclude = net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration.class)
@ActiveProfiles("test")
class RatingServiceApplicationTests {
    @Test
    void contextLoads() {}
}