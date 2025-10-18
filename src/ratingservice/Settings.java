package ratingservice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public final class Settings {

  private Settings() {}

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
          optional(
              "SPRING_DATASOURCE_USERNAME", values, optional("DATABASE_USERNAME", values, null));
      String databasePassword =
          optional(
              "SPRING_DATASOURCE_PASSWORD", values, optional("DATABASE_PASSWORD", values, null));

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

  public static final class DataSourceFactory {

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

  private static final class DatabaseUrlParser {

    private static final String POSTGRESQL_SCHEME = "postgresql";
    private static final int DEFAULT_PORT = 5432;

    private DatabaseUrlParser() {
      // Utility class
    }

    static DatabaseUrlComponents parse(final String rawUrl) {
      if (!hasText(rawUrl)) {
        throw new IllegalArgumentException("DATABASE_URL must be provided");
      }

      URI uri = createUri(rawUrl.trim());
      validateScheme(uri);

      String host = uri.getHost();
      if (!hasText(host)) {
        throw new IllegalArgumentException("DATABASE_URL must include host");
      }

      int port = uri.getPort() >= 0 ? uri.getPort() : DEFAULT_PORT;

      String database = extractDatabase(uri.getPath());

      String username = null;
      String password = null;
      String userInfo = uri.getUserInfo();
      if (hasText(userInfo)) {
        int colonIndex = userInfo.indexOf(':');
        if (colonIndex >= 0) {
          username = userInfo.substring(0, colonIndex);
          password = userInfo.substring(colonIndex + 1);
        } else {
          username = userInfo;
        }
        if (!hasText(username)) {
          throw new IllegalArgumentException("DATABASE_URL username cannot be empty");
        }
      }

      Map<String, String> parameters = parseQuery(uri.getQuery());

      return new DatabaseUrlComponents(host, port, database, username, password, parameters);
    }

    private static URI createUri(final String url) {
      try {
        return new URI(url);
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Invalid DATABASE_URL", e);
      }
    }

    private static void validateScheme(final URI uri) {
      if (!POSTGRESQL_SCHEME.equalsIgnoreCase(uri.getScheme())) {
        throw new IllegalArgumentException("Unsupported DATABASE_URL scheme: " + uri.getScheme());
      }
    }

    private static String extractDatabase(final String path) {
      if (!hasText(path) || "/".equals(path)) {
        throw new IllegalArgumentException("DATABASE_URL must include database name");
      }
      String database = path.startsWith("/") ? path.substring(1) : path;
      return URLDecoder.decode(database, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQuery(final String query) {
      if (!hasText(query)) {
        return Collections.emptyMap();
      }
      Map<String, String> params = new LinkedHashMap<>();
      for (String pair : query.split("&")) {
        if (!hasText(pair)) {
          continue;
        }
        int equalsIndex = pair.indexOf('=');
        String key;
        String value;
        if (equalsIndex >= 0) {
          key = pair.substring(0, equalsIndex);
          value = pair.substring(equalsIndex + 1);
        } else {
          key = pair;
          value = "";
        }
        key = URLDecoder.decode(key, StandardCharsets.UTF_8);
        value = URLDecoder.decode(value, StandardCharsets.UTF_8);
        params.put(key, value);
      }
      return Collections.unmodifiableMap(params);
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

    record DatabaseUrlComponents(
        String host,
        int port,
        String database,
        String username,
        String password,
        Map<String, String> parameters) {}
  }

  public static final class LoggingConfigurator {

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
        rootLogger.setLevel(Level.INFO);
      }
    }
  }
}
