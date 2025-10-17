package ratingservice.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Application configuration loaded from environment variables or a local .env file.
 */
public record AppConfig(
    String databaseUrl,
    String databaseUsername,
    String databasePassword,
    int grpcPort,
    String logLevel,
    String serviceName,
    String environment,
    int workerThreads) {

  private static final int DEFAULT_GRPC_PORT = 6565;
  private static final String DEFAULT_LOG_LEVEL = "INFO";
  private static final String DEFAULT_SERVICE_NAME = "rating-service";
  private static final String DEFAULT_ENVIRONMENT = "development";
  private static final int DEFAULT_WORKER_THREADS =
      Math.max(4, Runtime.getRuntime().availableProcessors());

  public static AppConfig load() {
    Map<String, String> values = loadEnvironment();

    String databaseUrl = require("DATABASE_URL", values);
    String databaseUsername =
        optional("SPRING_DATASOURCE_USERNAME", values, optional("DATABASE_USERNAME", values, null));
    String databasePassword =
        optional("SPRING_DATASOURCE_PASSWORD", values, optional("DATABASE_PASSWORD", values, null));

    int grpcPort = parseInt(optional("PORT", values, null), DEFAULT_GRPC_PORT, "PORT");
    String logLevel = optional("LOG_LEVEL", values, DEFAULT_LOG_LEVEL).toUpperCase(Locale.ROOT);
    String serviceName = optional("SPRING_APPLICATION_NAME", values, DEFAULT_SERVICE_NAME);
    String environment = optional("ENVIRONMENT", values, DEFAULT_ENVIRONMENT);

    int workerThreads =
        parseInt(
            optional("WORKER_THREADS", values, null), DEFAULT_WORKER_THREADS, "WORKER_THREADS");

    return new AppConfig(
        databaseUrl,
        blankToNull(databaseUsername),
        blankToNull(databasePassword),
        grpcPort,
        logLevel,
        serviceName,
        environment,
        workerThreads);
  }

  private static Map<String, String> loadEnvironment() {
    Map<String, String> env = new HashMap<>(System.getenv());
    Path envFile = Path.of(".env");
    if (Files.exists(envFile)) {
      try (Stream<String> lines = Files.lines(envFile, StandardCharsets.UTF_8)) {
        lines
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .filter(line -> !line.startsWith("#"))
            .forEach(
                line -> {
                  int equalsIndex = line.indexOf('=');
                  if (equalsIndex <= 0) {
                    return;
                  }
                  String key = line.substring(0, equalsIndex).trim();
                  String value = line.substring(equalsIndex + 1).trim();
                  value = stripEnclosingQuotes(value);
                  env.putIfAbsent(key, value);
                });
      } catch (IOException e) {
        throw new IllegalStateException("Failed to read .env file", e);
      }
    }
    return env;
  }

  private static String stripEnclosingQuotes(final String value) {
    if (value.length() >= 2) {
      char first = value.charAt(0);
      char last = value.charAt(value.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }

  private static String require(final String key, final Map<String, String> values) {
    String value = values.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Environment variable '" + key + "' must be provided");
    }
    return value.trim();
  }

  private static String optional(
      final String key, final Map<String, String> values, final String defaultValue) {
    String value = values.get(key);
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    return value.trim();
  }

  private static int parseInt(final String value, final int defaultValue, final String name) {
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid integer value for '" + name + "'", e);
    }
  }

  private static String blankToNull(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value;
  }
}

