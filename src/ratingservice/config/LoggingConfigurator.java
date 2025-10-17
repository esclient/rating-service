package ratingservice.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class LoggingConfigurator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingConfigurator.class);

  private final String rootLogLevel;
  private final String serviceName;
  private final String environment;

  public LoggingConfigurator(
      final String rootLogLevel, final String serviceName, final String environment) {
    this.rootLogLevel = rootLogLevel;
    this.serviceName = serviceName;
    this.environment = environment;
  }

  public void configure() {
    MDC.put("service", serviceName);
    MDC.put("environment", environment);

    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    context.reset();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(context);
    encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n");
    encoder.start();

    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(context);
    consoleAppender.setEncoder(encoder);
    consoleAppender.start();

    ch.qos.logback.classic.Logger rootLogger =
        context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.addAppender(consoleAppender);

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
}

