package com.esclient.ratingservice.service;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.resources.AuthClient;
import com.infisical.sdk.resources.SecretsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InfisicalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfisicalService.class);
    
    private final InfisicalSdk sdk;
    private final SecretsClient secretsClient;
    
    public InfisicalService(
            @Value("${INFISICAL_HOST:https://app.infisical.com}") String siteUrl,
            @Value("${INFISICAL_SERVER_TOKEN:}") String accessToken,
            @Value("${INFISICAL_CLIENT_ID:}") String clientId,
            @Value("${INFISICAL_CLIENT_SECRET:}") String clientSecret) {
        try {
            // SDK configuration with builder
            SdkConfig config = new SdkConfig.Builder()
                    .withSiteUrl(siteUrl)
                    .build();
            
            this.sdk = new InfisicalSdk(config);
            
            // Authenticate based on available credentials
            AuthClient authClient = sdk.Auth();
            
            if (accessToken != null && !accessToken.trim().isEmpty()) {
                // Use Access Token Auth - based on documentation this should be accessTokenAuth()
                authClient.SetAccessToken(accessToken);
                LOGGER.info("Authenticated using access token");
            } else if (clientId != null && clientSecret != null && 
                       !clientId.trim().isEmpty() && !clientSecret.trim().isEmpty()) {
                // Use Universal Auth - based on documentation this should be universalAuth()
                authClient.UniversalAuthLogin(clientId, clientSecret);
                LOGGER.info("Authenticated using universal auth");
            } else {
                throw new IllegalArgumentException("Either INFISICAL_SERVER_TOKEN or both INFISICAL_CLIENT_ID and INFISICAL_CLIENT_SECRET are required");
            }
            
            // Initialize SecretsClient
            this.secretsClient = sdk.Secrets();
            
            LOGGER.info("Infisical SDK initialized and authenticated successfully with host: {}", siteUrl);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Infisical SDK: {}", e.getMessage(), e);
            throw new RuntimeException("Infisical initialization failed", e);
        }
    }
    
    /**
     * Get secret with project ID, environment, and default path "/"
     */
    public String getSecret(String secretName, String projectId, String environment) {
        return getSecret(secretName, projectId, environment, "/");
    }
    
    /**
     * Get secret with project ID, environment, and custom path
     */
    public String getSecret(String secretName, String projectId, String environment, String secretPath) {
        try {
            // Validate parameters
            if (secretName == null || secretName.trim().isEmpty()) {
                throw new IllegalArgumentException("Secret name cannot be null or empty");
            }
            if (projectId == null || projectId.trim().isEmpty()) {
                throw new IllegalArgumentException("Project ID cannot be null or empty");
            }
            if (environment == null || environment.trim().isEmpty()) {
                throw new IllegalArgumentException("Environment cannot be null or empty");
            }
            if (secretPath == null || secretPath.trim().isEmpty()) {
                secretPath = "/";
            }
            
            // Based on the documentation, use getSecretByName method
            Secret secret = secretsClient.GetSecret(secretName, projectId, environment, secretPath, false, false, null);
            
            if (secret != null && secret.getSecretValue() != null) {
                LOGGER.debug("Successfully retrieved secret: {} from project: {} environment: {} path: {}", 
                           secretName, projectId, environment, secretPath);
                return secret.getSecretValue();
            } else {
                LOGGER.warn("Secret '{}' not found or has null value", secretName);
                throw new RuntimeException("Secret '" + secretName + "' not found or has null value");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve secret '{}' from project '{}' environment '{}' path '{}': {}", 
                        secretName, projectId, environment, secretPath, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve secret: " + secretName, e);
        }
    }
}