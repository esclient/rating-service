package com.esclient.ratingservice.repository;

import java.sql.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class RatingRepository {
  private final DataSource dataSource;

  public RatingRepository(DataSource dataSource) { // Use DataSource instead of Connection
    this.dataSource = dataSource;
  }

  public long addRate(long modId, long authorId, int rate) throws SQLException {
    String sql = "INSERT INTO rates (author_id, mod_id, rate) VALUES (?, ?, ?)";

    try (Connection conn = dataSource.getConnection(); // Get connection from DataSource
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setLong(1, authorId);
      stmt.setLong(2, modId);
      stmt.setInt(3, rate);
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

  public long getTotalRates(long modId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM rates WHERE mod_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, modId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getLong(1) : 0;
      }
    }
  }

  public long getRates(long rate, long modId) throws SQLException {
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
