package com.esclient.ratingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
public class RatingServiceApplication implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingServiceApplication.class);
    
    @Value("${grpc.server.port}")
    private int grpcPort;
    
    public static void main(final String[] args) {
        SpringApplication.run(RatingServiceApplication.class, args);
    }
    
    @Override
    public void run(String... args) {
        logger.info("Rating Service started successfully");
        logger.info("gRPC server listening on port: {}", grpcPort);
        logger.debug("Application startup completed with args: {}", (Object[]) args);
    }
}