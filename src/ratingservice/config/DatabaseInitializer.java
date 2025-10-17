package ratingservice.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

  private static final String CREATE_TABLE_SQL =
      """
      CREATE TABLE IF NOT EXISTS rates (
          id BIGSERIAL PRIMARY KEY,
          mod_id BIGINT NOT NULL,
          author_id BIGINT NOT NULL,
          rate INTEGER NOT NULL CHECK (rate >= 1 AND rate <= 5),
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          UNIQUE(mod_id, author_id)
      )
      """;

  private static final String CREATE_MOD_ID_INDEX_SQL =
      "CREATE INDEX IF NOT EXISTS idx_ratings_mod_id ON rates(mod_id)";
  private static final String CREATE_RATE_INDEX_SQL =
      "CREATE INDEX IF NOT EXISTS idx_ratings_rate ON rates(rate)";

  public void initialize(final DataSource dataSource) {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(CREATE_TABLE_SQL);
      statement.execute(CREATE_MOD_ID_INDEX_SQL);
      statement.execute(CREATE_RATE_INDEX_SQL);
      LOGGER.info("Database schema verified successfully");
    } catch (SQLException e) {
      throw new IllegalStateException("Failed to initialize database schema", e);
    }
  }
}

