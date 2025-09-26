package com.esclient.ratingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import com.esclient.ratingservice.service.InfisicalService;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JdbcTemplateAutoConfiguration.class,
})
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

  @Autowired
  private InfisicalService infisicalService;

  @Autowired
  private ConfigurableApplicationContext applicationContext;

  public static void main(final String[] args) {
    SpringApplication.run(RatingServiceApplication.class, args);
  }

  @Override
  public final void run(final String... args) {
      LOGGER.info("Rating Service started successfully");
      LOGGER.info("gRPC server listening on port: {}", grpcPort);
    
      try {
          infisicalService.getSecret("DATABASE_URL", infisicalProjectId, infisicalEnvironment, infisicalSecretPath);
          LOGGER.info("Retrieved secret from Infisical");
      } catch (Exception e) {
          LOGGER.error("Failed to retrieve secret from Infisical: {}", e.getMessage());
          LOGGER.error("Application cannot continue without required secrets. Shutting down...");
          SpringApplication.exit(applicationContext, () -> 1);
          return;
      }
    
      LOGGER.debug("Application startup completed with args: {}", (Object[]) args);
  }
}
