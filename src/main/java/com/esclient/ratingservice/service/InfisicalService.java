package com.esclient.ratingservice.service;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.SdkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.jdbc.SqlConfig;

@Service
public class InfisicalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfisicalService.class);
    
    private final InfisicalSdk sdk;
    
    public InfisicalService(
        @Value("${infisical.site-url:https://app.infisical.com}") String siteUrl,
        @Value("${infisical.client-id}") String clientId,
        @Value("${infisical.client-secret}") String clientSecret) {
        
        try {
            this.sdk = new InfisicalSdk(
                new SqlConfig.Builder()
                    .withSiteUrl(siteUrl)
                    .build()
            );
        
            // Authenticate on service initialization
            sdk.Auth().UniversalAuthLogin(clientId, clientSecret);
            LOGGER.info("Infisical SDK initialized and authenticated successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Infisical SDK: {}", e.getMessage(), e);
            throw new RuntimeException("Infisical initialization failed", e);
        }
    }
    
   public Object getSecret(String secretName, String projectId, String environment, String path) {
        try {
            var secret = sdk.Secrets().GetSecret(secretName, projectId, environment, path, null, null, null);
            LOGGER.debug("Successfully retrieved secret: {}", secretName);
            return secret;
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve secret '{}': {}", secretName, e.getMessage());
            throw new RuntimeException("Failed to retrieve secret: " + secretName, e);
        }   
    }
}
