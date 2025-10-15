package com.esclient.ratingservice.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

final class DatabaseUrlParser {

  private static final String POSTGRESQL_SCHEME = "postgresql";
  private static final int DEFAULT_PORT = 5432;

  private DatabaseUrlParser() {}

  static DatabaseUrlComponents parse(final String rawUrl) {
    if (!StringUtils.hasText(rawUrl)) {
      throw new IllegalArgumentException("DATABASE_URL must be provided");
    }

    URI uri = createUri(rawUrl.trim());
    validateScheme(uri);

    String host = uri.getHost();
    if (!StringUtils.hasText(host)) {
      throw new IllegalArgumentException("DATABASE_URL must include host");
    }

    int port = uri.getPort() >= 0 ? uri.getPort() : DEFAULT_PORT;

    String database = extractDatabase(uri.getPath());

    String username = null;
    String password = null;
    String userInfo = uri.getUserInfo();
    if (StringUtils.hasText(userInfo)) {
      int colonIndex = userInfo.indexOf(':');
      if (colonIndex >= 0) {
        username = userInfo.substring(0, colonIndex);
        password = userInfo.substring(colonIndex + 1);
      } else {
        username = userInfo;
      }
      if (!StringUtils.hasText(username)) {
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
    if (!StringUtils.hasText(path) || "/".equals(path)) {
      throw new IllegalArgumentException("DATABASE_URL must include database name");
    }
    String database = path.startsWith("/") ? path.substring(1) : path;
    return URLDecoder.decode(database, StandardCharsets.UTF_8);
  }

  private static Map<String, String> parseQuery(final String query) {
    if (!StringUtils.hasText(query)) {
      return Collections.emptyMap();
    }
    Map<String, String> params = new LinkedHashMap<>();
    for (String pair : query.split("&")) {
      if (!StringUtils.hasText(pair)) {
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

  record DatabaseUrlComponents(
      String host, int port, String database, String username, String password, Map<String, String> parameters) {}
}
