package ratingservice.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import ratingservice.constants.Constants;
import ratingservice.model.Data;

public class Repository {
  private final DataSource dataSource;

  public Repository(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public long addRate(final long modId, final long authorId, final int rate) throws SQLException {
    String sql = "INSERT INTO rates (author_id, mod_id, rate) VALUES (?, ?, ?)";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      final int paramRateIndex = 3;
      stmt.setLong(1, authorId);
      stmt.setLong(2, modId);
      stmt.setInt(paramRateIndex, rate);
      int affectedRows = stmt.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Creating rating failed, no rows affected.");
      }
      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          return generatedKeys.getLong(1);
        } else {
          throw new SQLException("Creating rating failed, no ID obtained.");
        }
      }
    }
  }

  public Data getRatingSummary(final long modId) throws SQLException {
    String sql =
        """
        SELECT COUNT(*) AS total,
               SUM(CASE WHEN rate = ? THEN 1 ELSE 0 END) AS rate1,
               SUM(CASE WHEN rate = ? THEN 1 ELSE 0 END) AS rate2,
               SUM(CASE WHEN rate = ? THEN 1 ELSE 0 END) AS rate3,
               SUM(CASE WHEN rate = ? THEN 1 ELSE 0 END) AS rate4,
               SUM(CASE WHEN rate = ? THEN 1 ELSE 0 END) AS rate5
          FROM rates
         WHERE mod_id = ?
        """;
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, Constants.RATE_1);
      stmt.setInt(2, Constants.RATE_2);
      stmt.setInt(3, Constants.RATE_3);
      stmt.setInt(4, Constants.RATE_4);
      stmt.setInt(5, Constants.RATE_5);
      stmt.setLong(6, modId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          long total = rs.getLong("total");
          long rate1 = rs.getLong("rate1");
          long rate2 = rs.getLong("rate2");
          long rate3 = rs.getLong("rate3");
          long rate4 = rs.getLong("rate4");
          long rate5 = rs.getLong("rate5");
          return new Data(total, rate1, rate2, rate3, rate4, rate5);
        }
        return new Data(0, 0, 0, 0, 0, 0);
      }
    }
  }
}
