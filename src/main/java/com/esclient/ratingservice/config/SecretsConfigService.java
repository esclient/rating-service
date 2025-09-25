package com.esclient.ratingservice.config;

import com.esclient.ratingservice.service.InfisicalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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
        
        LOGGER.info("Secrets configuration service initialized for project: {} environment: {}", 
                   projectId, environment);
    }
   
    /**
     * Retrieve a secret from Infisical using the configured project and environment
     */
    public String getSecret(String secretName) {
        return getSecret(secretName, "/");
    }
    
    /**
     * Retrieve a secret from Infisical with custom path
     */
    public String getSecret(String secretName, String path) {
        try {
            return infisicalService.getSecret(secretName, projectId, environment, path);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve secret '{}': {}", secretName, e.getMessage());
            return null;
        }
    }
}