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
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourceConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);

  private final InfisicalService infisicalService;

  private static final int PROTOCOL_SEPARATOR_LENGTH = 3;

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
    String databaseUrl = null;

    try {
      LOGGER.info("Attempting to configure DataSource with Infisical secrets...");
      LOGGER.info(
          "Looking for DATABASE_URL in project: {}, environment: {}, path: {}",
          infisicalProjectId,
          infisicalEnvironment,
          infisicalSecretPath);

      databaseUrl =
          infisicalService.getSecret(
              "DATABASE_URL", infisicalProjectId, infisicalEnvironment, infisicalSecretPath);

      if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
        LOGGER.info("Successfully retrieved DATABASE_URL from Infisical");
      } else {
        throw new RuntimeException("DATABASE_URL secret is null or empty");
      }

    } catch (Exception e) {
      LOGGER.warn(
          "Failed to retrieve DATABASE_URL from Infisical: {}. Using fallback configuration.",
          e.getMessage());

      // Use fallback values
      databaseUrl = fallbackDatabaseUrl;
      LOGGER.info("Using fallback database configuration: {}", maskUrl(databaseUrl));
    }

    try {
      // Parse the PostgreSQL URL to extract components
      DatabaseUrlComponents components = parsePostgreSQLUrl(databaseUrl);

      DataSourceBuilder<?> builder =
          DataSourceBuilder.create()
              .url(components.jdbcUrl)
              .username(components.username)
              .password(components.password)
              .driverClassName("org.postgresql.Driver");

      DataSource dataSource = builder.build();
      LOGGER.info("DataSource configured successfully");
      return dataSource;

    } catch (Exception e) {
      LOGGER.error("Failed to create DataSource: {}", e.getMessage());
      throw new RuntimeException("Failed to configure DataSource", e);
    }
  }

  private DatabaseUrlComponents parsePostgreSQLUrl(final String url) {
    try {
      // Handle postgresql:// URLs
      if (url.startsWith("postgresql://")) {
        // Remove the postgresql:// prefix
        String urlWithoutProtocol = url.substring("postgresql://".length());

        String username = null;
        String password = null;
        String hostPortDb;

        // Check if credentials are present
        if (urlWithoutProtocol.contains("@")) {
          String[] credentialsAndHost = urlWithoutProtocol.split("@", 2);
          String credentials = credentialsAndHost[0];
          hostPortDb = credentialsAndHost[1];

          if (credentials.contains(":")) {
            String[] userPass = credentials.split(":", 2);
            username = userPass[0];
            password = userPass[1];
          } else {
            username = credentials;
          }
        } else {
          hostPortDb = urlWithoutProtocol;
        }

        // Construct JDBC URL
        String jdbcUrl = "jdbc:postgresql://" + hostPortDb;

        return new DatabaseUrlComponents(jdbcUrl, username, password);
      }

      // If already in JDBC format, try to parse it
      if (url.startsWith("jdbc:postgresql://")) {
        return new DatabaseUrlComponents(url, null, null);
      }

      // Fallback - assume it needs jdbc: prefix
      return new DatabaseUrlComponents("jdbc:" + url, null, null);

    } catch (Exception e) {
      LOGGER.error("Failed to parse database URL: {}", e.getMessage());
      throw new RuntimeException("Invalid database URL format", e);
    }
  }

  private record DatabaseUrlComponents(String jdbcUrl, String username, String password) {}

  /** Convert postgresql:// URLs to jdbc:postgresql:// format */
  private String convertToJdbcUrl(final String url) {
    if (url == null) {
      return null;
    }
    LOGGER.debug("Converting URL: {}", maskUrl(url));

    // Convert various URL formats to JDBC format
    if (url.startsWith("postgresql://")) {
      String jdbcUrl = "jdbc:" + url;
      LOGGER.debug("Converted to JDBC URL: {}", maskUrl(jdbcUrl));
      return jdbcUrl;
    } else if (url.startsWith("mysql://")) {
      return "jdbc:" + url;
    } else if (url.startsWith("jdbc:")) {
      return url; // Already in JDBC format
    }

    // If no known prefix, assume it needs jdbc: prefix
    return url.startsWith("jdbc:") ? url : "jdbc:" + url;
  }

  @Bean
  public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  private String determineDriverClassName(final String databaseUrl) {
    if (databaseUrl.startsWith("jdbc:postgresql:")) {
      return "org.postgresql.Driver";
    } else if (databaseUrl.startsWith("jdbc:mysql:")) {
      return "com.mysql.cj.jdbc.Driver";
    } else if (databaseUrl.startsWith("jdbc:h2:")) {
      return "org.h2.Driver";
    } else if (databaseUrl.startsWith("jdbc:sqlserver:")) {
      return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }
    return null;
  }

  private String maskUrl(final String url) {
    if (url == null) {
      return "null";
    }
    // Simple masking for security
    if (url.contains("://") && url.contains("@")) {
      int protocolEnd = url.indexOf("://") + PROTOCOL_SEPARATOR_LENGTH;
      int atIndex = url.lastIndexOf("@");
      return url.substring(0, protocolEnd) + "***:***" + url.substring(atIndex);
    }
    return url;
  }
}
