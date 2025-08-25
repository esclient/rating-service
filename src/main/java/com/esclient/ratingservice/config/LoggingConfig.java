package com.esclient.ratingservice.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggingConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingConfig.class);

  @Value("${logging.level.root:INFO}")
  private String rootLogLevel;

  @Value("${spring.application.name:rating-service}")
  private String serviceName;

  @Value("${ENVIRONMENT:development}")
  private String environment;

  @PostConstruct
  public void configureLogging() {
    // Set up MDC with service-wide context
    MDC.put("service", serviceName);
    MDC.put("environment", environment);

    // Configure root logger level programmatically if needed
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    ch.qos.logback.classic.Logger rootLogger =
        context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

    try {
      Level level = Level.valueOf(rootLogLevel.toUpperCase(Locale.ROOT));
      rootLogger.setLevel(level);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Invalid log level '{}', using default INFO", rootLogLevel);
      rootLogger.setLevel(Level.INFO);
    }

    LOGGER.info(
        "Logging configured successfully - Level: {}, Service: {}, Environment: {}",
        rootLogLevel,
        serviceName,
        environment);
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getEnvironment() {
    return environment;
  }
}
