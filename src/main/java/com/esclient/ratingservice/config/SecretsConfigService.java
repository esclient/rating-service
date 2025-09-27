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
      final InfisicalService infisicalService,
      @Value("${INFISICAL_PROJECT_ID}") final String projectId,
      @Value("${INFISICAL_ENVIRONMENT}") final String environment) {

    // Validate required parameters
    if (projectId == null || projectId.trim().isEmpty()) {
      throw new IllegalArgumentException("INFISICAL_PROJECT_ID is required");
    }
    if (environment == null || environment.trim().isEmpty()) {
      throw new IllegalArgumentException("INFISICAL_ENVIRONMENT is required");
    }

    this.infisicalService = infisicalService;
    this.projectId = projectId;
    this.environment = environment;

    LOGGER.info(
        "Secrets configuration service initialized for project: {} environment: {}",
        projectId,
        environment);
  }

  /**
   * Retrieve a secret from Infisical using the configured project and environment with default path
   * "/"
   */
  public String getSecret(final String secretName) {
    return getSecret(secretName, "/");
  }

  /** Retrieve a secret from Infisical with custom path */
  public String getSecret(final String secretName, final String path) {
    try {
      if (secretName == null || secretName.trim().isEmpty()) {
        LOGGER.error("Secret name cannot be null or empty");
        return null;
      }

      // Use the 4-parameter method with path support
      String secretValue = infisicalService.getSecret(secretName, projectId, environment, path);
      LOGGER.debug("Successfully retrieved secret '{}' from path '{}'", secretName, path);
      return secretValue;
    } catch (Exception e) {
      LOGGER.error(
          "Failed to retrieve secret '{}' from path '{}': {}", secretName, path, e.getMessage());
      return null;
    }
  }

  /**
   * Retrieve a secret from Infisical using default path "/" with exception throwing Use this when
   * you want to handle the exception in the calling code
   */
  public String getSecretOrThrow(final String secretName) throws RuntimeException {
    return getSecretOrThrow(secretName, "/");
  }

  /** Retrieve a secret from Infisical with custom path with exception throwing */
  public String getSecretOrThrow(final String secretName, final String path)
      throws RuntimeException {
    if (secretName == null || secretName.trim().isEmpty()) {
      throw new IllegalArgumentException("Secret name cannot be null or empty");
    }

    // Use the 4-parameter method with path support
    return infisicalService.getSecret(secretName, projectId, environment, path);
  }

  /** Get the configured project ID */
  public String getProjectId() {
    return projectId;
  }

  /** Get the configured environment */
  public String getEnvironment() {
    return environment;
  }
}
