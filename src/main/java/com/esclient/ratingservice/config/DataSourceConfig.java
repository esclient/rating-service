package com.esclient.ratingservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Profile("!test")
public class DataSourceConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);

  private final String databaseUrl;
  private final String fallbackUsername;
  private final String fallbackPassword;

  public DataSourceConfig(
      @Value("${DATABASE_URL:}") final String databaseUrl,
      @Value("${spring.datasource.username:}") final String fallbackUsername,
      @Value("${spring.datasource.password:}") final String fallbackPassword) {
    this.databaseUrl = databaseUrl;
    this.fallbackUsername = fallbackUsername;
    this.fallbackPassword = fallbackPassword;
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public HikariConfig hikariConfig() {
    return new HikariConfig();
  }

  @Bean
  @Primary
  public DataSource dataSource(final HikariConfig hikariConfig) {
    DatabaseUrlParser.DatabaseUrlComponents components = DatabaseUrlParser.parse(databaseUrl);

    PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
    pgDataSource.setServerNames(new String[] {components.host()});
    pgDataSource.setPortNumbers(new int[] {components.port()});
    pgDataSource.setDatabaseName(components.database());

    String username = firstNonBlank(components.username(), fallbackUsername);
    if (StringUtils.hasText(username)) {
      pgDataSource.setUser(username);
    }

    String password = components.password() != null ? components.password() : fallbackPassword;
    if (password != null) {
      pgDataSource.setPassword(password);
    }

    components
        .parameters()
        .forEach(
            (key, value) -> {
              try {
                pgDataSource.setProperty(key, value);
              } catch (SQLException e) {
                throw new IllegalArgumentException(
                    "Invalid parameter in DATABASE_URL: " + key, e);
              }
            });

    hikariConfig.setDataSource(pgDataSource);

    LOGGER.info(
        "Configured DataSource for {}:{} / {}",
        components.host(),
        components.port(),
        components.database());

    return new HikariDataSource(hikariConfig);
  }

  private String firstNonBlank(final String primary, final String secondary) {
    if (StringUtils.hasText(primary)) {
      return primary;
    }
    if (StringUtils.hasText(secondary)) {
      return secondary;
    }
    return null;
  }

}
