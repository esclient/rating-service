package com.esclient.ratingservice.config;

import com.esclient.ratingservice.service.InfisicalService;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("!test")
public class DataSourceConfig {

  // Static fields first
  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);
  private static final int PROTOCOL_SEPARATOR_LENGTH = 3;
  private static final String JDBC_PREFIX = "jdbc:"; // Fix duplicate literals
  private static final String POSTGRESQL_PREFIX = "postgresql://";
  private static final String JDBC_POSTGRESQL_PREFIX = "jdbc:postgresql://";

  // Instance fields after static fields
  private final InfisicalService infisicalService;

  @Value("${infisical.project-id}")
  private String infisicalProjectId;

  @Value("${infisical.environment}")
  private String infisicalEnvironment;

  @Value("${infisical.secret-path}")
  private String infisicalSecretPath;

  // Fallback properties
  @Value("${DATABASE_URL:jdbc:h2:mem:testdb}")
  private String fallbackDatabaseUrl;

  public DataSourceConfig(final InfisicalService infisicalService) {
    this.infisicalService = infisicalService;
  }

  @Bean
  @Primary
  public DataSource dataSource() {
    String databaseUrl = retrieveDatabaseUrl();
    return createDataSource(databaseUrl);
  }

  private String retrieveDatabaseUrl() {
    try {
      LOGGER.info("Attempting to configure DataSource with Infisical secrets...");

      String lookupMessage =
          String.format(
              "Looking for DATABASE_URL in project: %s, environment: %s, path: %s",
              infisicalProjectId, infisicalEnvironment, infisicalSecretPath);
      LOGGER.info(lookupMessage);

      String databaseUrl =
          infisicalService.getSecret(
              "DATABASE_URL", infisicalProjectId, infisicalEnvironment, infisicalSecretPath);

      if (databaseUrl != null && !databaseUrl.isBlank()) {
        LOGGER.info("Successfully retrieved DATABASE_URL from Infisical");
        return databaseUrl;
      } else {
        throw new DataSourceConfigurationException(
            "DATABASE_URL secret is null or empty from Infisical service");
      }

    } catch (IllegalArgumentException e) {
      LOGGER.warn(
          "Invalid configuration for Infisical: {}. Using fallback configuration.", e.getMessage());
      return fallbackDatabaseUrl;
    } catch (RuntimeException e) {
      LOGGER.warn(
          "Failed to retrieve DATABASE_URL from Infisical: {}. Using fallback configuration.",
          e.getMessage());
      LOGGER.info("Using fallback database configuration: {}", maskUrl(fallbackDatabaseUrl));
      return fallbackDatabaseUrl;
    }
  }

  private DataSource createDataSource(final String databaseUrl) {
    try {
      DatabaseUrlComponents components = parsePostgreSQLUrl(databaseUrl);

      DataSourceBuilder<?> builder =
          DataSourceBuilder.create()
              .url(components.jdbcUrl())
              .username(components.username())
              .password(components.password())
              .driverClassName("org.postgresql.Driver");

      DataSource dataSource = builder.build();
      LOGGER.info("DataSource configured successfully");
      return dataSource;

    } catch (IllegalArgumentException e) {
      LOGGER.error("Failed to create DataSource: {}", e.getMessage());
      throw new DataSourceConfigurationException(
          "Invalid database configuration parameters: " + e.getMessage(), e);
    }
  }

  private DatabaseUrlComponents parsePostgreSQLUrl(final String url) {
    if (url == null || url.isBlank()) {
      throw new DataSourceConfigurationException("Database URL cannot be null or blank");
    }

    if (url.startsWith(POSTGRESQL_PREFIX)) {
      return parsePostgresUrl(url);
    }

    if (url.startsWith(JDBC_POSTGRESQL_PREFIX)) {
      return new DatabaseUrlComponents(url, null, null);
    }

    // Fallback - assume it needs jdbc: prefix
    return new DatabaseUrlComponents(JDBC_PREFIX + url, null, null);
  }

  private DatabaseUrlComponents parsePostgresUrl(final String url) {
    // Remove the postgresql:// prefix
    String urlWithoutProtocol = url.substring(POSTGRESQL_PREFIX.length());

    if (urlWithoutProtocol.contains("@")) {
      return parseUrlWithCredentials(urlWithoutProtocol);
    } else {
      // No credentials in URL
      String jdbcUrl = JDBC_POSTGRESQL_PREFIX + urlWithoutProtocol;
      return new DatabaseUrlComponents(jdbcUrl, null, null);
    }
  }

  private DatabaseUrlComponents parseUrlWithCredentials(final String urlWithoutProtocol) {
    String[] credentialsAndHost = urlWithoutProtocol.split("@", 2);
    if (credentialsAndHost.length != 2) {
      throw new DataSourceConfigurationException("Invalid URL format: missing host after @");
    }

    String credentials = credentialsAndHost[0];
    String hostPortDb = credentialsAndHost[1];

    String username;
    String password = null;

    if (credentials.contains(":")) {
      String[] userPass = credentials.split(":", 2);
      username = userPass[0];
      password = userPass[1];
    } else {
      username = credentials;
    }

    String jdbcUrl = JDBC_POSTGRESQL_PREFIX + hostPortDb;
    return new DatabaseUrlComponents(jdbcUrl, username, password);
  }

  private record DatabaseUrlComponents(String jdbcUrl, String username, String password) {}

  @Bean
  public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  private String maskUrl(final String url) {
    if (url == null) {
      return "null";
    }

    if (url.contains("://") && url.contains("@")) {
      int protocolEnd = url.indexOf("://") + PROTOCOL_SEPARATOR_LENGTH;
      int atIndex = url.lastIndexOf('@');
      return url.substring(0, protocolEnd) + "***:***" + url.substring(atIndex);
    }
    return url;
  }

  @Override
  public String toString() {
    return "DataSourceConfig{"
        + "infisicalService="
        + infisicalService
        + ", infisicalProjectId='"
        + infisicalProjectId
        + '\''
        + ", infisicalEnvironment='"
        + infisicalEnvironment
        + '\''
        + ", infisicalSecretPath='"
        + infisicalSecretPath
        + '\''
        + ", fallbackDatabaseUrl='"
        + maskUrl(fallbackDatabaseUrl)
        + '\''
        + '}';
  }

  // Custom exception class with serialVersionUID
  public static class DataSourceConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataSourceConfigurationException(final String message) {
      super(message);
    }

    public DataSourceConfigurationException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}
