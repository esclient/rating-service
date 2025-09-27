package com.esclient.ratingservice;

import com.esclient.ratingservice.config.SecretsConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
public class RatingServiceApplication implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(RatingServiceApplication.class);

  @Value("${grpc.server.port}")
  private int grpcPort;

  @Value("${INFISICAL_PROJECT_ID}")
  private String infisicalProjectId;

  @Value("${INFISICAL_ENVIRONMENT}")
  private String infisicalEnvironment;

  @Value("${INFISICAL_SECRET_PATH}")
  private String infisicalSecretPath;

  @Autowired private SecretsConfigService secretsConfigService;

  @Autowired private ConfigurableApplicationContext applicationContext;

  public static void main(final String[] args) {
    SpringApplication.run(RatingServiceApplication.class, args);
  }

  @Override
  public final void run(final String... args) {
    LOGGER.info("Rating Service started successfully");
    LOGGER.info("gRPC server listening on port: {}", grpcPort);

    try {
      String testSecret = secretsConfigService.getSecret("DATABASE_URL", infisicalSecretPath);
      if (testSecret != null) {
        LOGGER.info("Successfully validated Infisical connection");
      }
    } catch (Exception e) {
      LOGGER.error("Failed to validate Infisical connection: {}", e.getMessage());
    }

    LOGGER.debug("Application startup completed with args: {}", (Object[]) args);
  }
}
