package ratingservice.config;

import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataSourceFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

  private DataSourceFactory() {
    // Utility class
  }

  public static DataSource create(final AppConfig config) {
    DatabaseUrlParser.DatabaseUrlComponents components =
        DatabaseUrlParser.parse(config.databaseUrl());

    PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
    pgDataSource.setServerNames(new String[] {components.host()});
    pgDataSource.setPortNumbers(new int[] {components.port()});
    pgDataSource.setDatabaseName(components.database());

    String username = firstNonBlank(components.username(), config.databaseUsername());
    if (hasText(username)) {
      pgDataSource.setUser(username);
    }

    String password =
        components.password() != null ? components.password() : config.databasePassword();
    if (password != null) {
      pgDataSource.setPassword(password);
    }

    applyParameters(pgDataSource, components.parameters());

    LOGGER.info(
        "Configured DataSource for {}:{} / {}",
        components.host(),
        components.port(),
        components.database());

    return pgDataSource;
  }

  private static void applyParameters(
      final PGSimpleDataSource pgDataSource, final Map<String, String> parameters) {
    parameters.forEach(
        (key, value) -> {
          try {
            pgDataSource.setProperty(key, value);
          } catch (SQLException e) {
            throw new IllegalArgumentException("Invalid parameter in DATABASE_URL: " + key, e);
          }
        });
  }

  private static String firstNonBlank(final String primary, final String secondary) {
    if (hasText(primary)) {
      return primary;
    }
    if (hasText(secondary)) {
      return secondary;
    }
    return null;
  }

  private static boolean hasText(final String value) {
    if (value == null) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isWhitespace(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }
}

