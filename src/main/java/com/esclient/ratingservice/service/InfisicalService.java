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
            @Value("${infisical.site-url:https://app.infisical.com}") String siteUrl,
            @Value("${infisical.client-id}") String clientId,
            @Value("${infisical.client-secret}") String clientSecret) {

        try {
            // SDK configuration with builder
            SdkConfig config = new SdkConfig.Builder()
                    .withSiteUrl(siteUrl)
                    .build();

            this.sdk = new InfisicalSdk(config);

            // Authenticate
            AuthClient authClient = sdk.Auth();
            authClient.universalAuthLogin(clientId, clientSecret);

            // Initialize SecretsClient
            this.secretsClient = sdk.Secrets();

            LOGGER.info("Infisical SDK initialized and authenticated successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Infisical SDK: {}", e.getMessage(), e);
            throw new RuntimeException("Infisical initialization failed", e);
        }
    }

    public String getSecret(String secretName, String projectId, String environment) {
        try {
            // Directly call getSecret with 3 arguments
            String value = secretsClient.getSecret(secretName, projectId, environment);
            if (value != null) {
                LOGGER.debug("Successfully retrieved secret: {}", secretName);
                return value;
            } else {
                throw new RuntimeException("Secret is null or empty");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve secret '{}': {}", secretName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve secret: " + secretName, e);
        }
    }
}
