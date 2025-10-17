package ratingservice.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

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

  public long getTotalRates(final long modId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM rates WHERE mod_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, modId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getLong(1) : 0;
      }
    }
  }

  public long getRates(final long rate, final long modId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM rates WHERE rate = ? AND mod_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, rate);
      stmt.setLong(2, modId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getLong(1) : 0;
      }
    }
  }
}

