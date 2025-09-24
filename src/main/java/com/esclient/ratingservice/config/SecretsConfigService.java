package com.esclient.ratingservice.config;

import com.esclient.ratingservice.service.InfisicalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

@Configuration
public class SecretsConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretsConfigService.class);
    
    private final InfisicalService infisicalService;
    private final String projectId;
    private final String environment;
    
    public SecretsConfigService(
        InfisicalService infisicalService,
        @Value("${infisical.project-id}") String projectId,
        @Value("${infisical.environment}") String environment) {
        
        this.infisicalService = infisicalService;
        this.projectId = projectId;
        this.environment = environment;
    }
    
    @Bean
    public DataSource dataSource() {
        try {
            // Get database URL from Infisical
            var dbUrlSecret = infisicalService.getSecret("DATABASE_URL", projectId, environment, "/");
            var dbUserSecret = infisicalService.getSecret("DATABASE_USER", projectId, environment, "/");
            var dbPasswordSecret = infisicalService.getSecret("DATABASE_PASSWORD", projectId, environment, "/");
            
            // Extract the actual values (you'll need to check Infisical SDK docs for the correct method)
            String dbUrl = extractSecretValue(dbUrlSecret);
            String dbUser = extractSecretValue(dbUserSecret);
            String dbPassword = extractSecretValue(dbPasswordSecret);
            
            LOGGER.info("Configuring datasource with secrets from Infisical");
            
            return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUser)
                .password(dbPassword)
                .driverClassName("org.postgresql.Driver")
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Failed to configure datasource with Infisical secrets: {}", e.getMessage());
            throw new RuntimeException("Database configuration failed", e);
        }
    }
    
    private String extractSecretValue(Object secret) {
        // You'll need to implement this based on the actual Infisical SDK response format
        // Check the SDK documentation for the correct way to extract the secret value
        if (secret != null) {
            return secret.toString(); // Replace with proper value extraction
        }
        throw new IllegalArgumentException("Secret value is null");
    }
    
    public String getSecret(String secretName) {
        try {
            var secret = infisicalService.getSecret(secretName, projectId, environment, "/");
            return extractSecretValue(secret);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve secret '{}': {}", secretName, e.getMessage());
            return null;
        }
    }
}