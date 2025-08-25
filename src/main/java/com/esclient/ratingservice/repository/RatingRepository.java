package com.esclient.ratingservice.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Repository;

@Repository
public class RatingRepository {
  
  private static final Logger logger = LoggerFactory.getLogger(RatingRepository.class);
  private final DataSource dataSource;

  public RatingRepository(final DataSource dataSource) { // Use DataSource instead of Connection
    this.dataSource = dataSource;
  }

  public long addRate(final long modId, final long authorId, final int rate) throws SQLException {
    // Add request-specific context to MDC
    MDC.put("modId", String.valueOf(modId));
    MDC.put("authorId", String.valueOf(authorId));
    MDC.put("dbOperation", "addRate");
    
    String sql = "INSERT INTO rates (author_id, mod_id, rate) VALUES (?, ?, ?)";
    
    try {
      logger.debug("Executing addRate query for modId: {}, authorId: {}, rate: {}", modId, authorId, rate);
      
      try (Connection conn = dataSource.getConnection(); // Get connection from DataSource
          PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        final int paramRateIndex = 3; // otherwise the 3 is a magic number
        stmt.setLong(1, authorId);
        stmt.setLong(2, modId);
        stmt.setInt(paramRateIndex, rate);
        
        int affectedRows = stmt.executeUpdate();
        
        if (affectedRows == 0) {
          logger.error("Failed to insert rating - no rows affected for modId: {}, authorId: {}", modId, authorId);
          throw new SQLException("Creating rating failed, no rows affected.");
        }
        
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            long generatedId = generatedKeys.getLong(1);
            logger.info("Successfully added rating with ID: {} for modId: {}, authorId: {}, rate: {}", 
                       generatedId, modId, authorId, rate);
            return generatedId;
          } else {
            logger.error("Failed to obtain generated ID for rating - modId: {}, authorId: {}", modId, authorId);
            throw new SQLException("Creating rating failed, no ID obtained.");
          }
        }
      }
    } catch (SQLException e) {
      logger.error("Database error while adding rating for modId: {}, authorId: {}, rate: {}", 
                   modId, authorId, rate, e);
      throw e;
    } finally {
      // Clean up MDC
      MDC.remove("modId");
      MDC.remove("authorId");
      MDC.remove("dbOperation");
    }
  }

  public long getTotalRates(final long modId) throws SQLException {
    // Add request-specific context to MDC
    MDC.put("modId", String.valueOf(modId));
    MDC.put("dbOperation", "getTotalRates");
    
    String sql = "SELECT COUNT(*) FROM rates WHERE mod_id = ?";
    
    try {
      logger.debug("Executing getTotalRates query for modId: {}", modId);
      
      try (Connection conn = dataSource.getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setLong(1, modId);
        
        try (ResultSet rs = stmt.executeQuery()) {
          long totalRates = rs.next() ? rs.getLong(1) : 0;
          logger.debug("Retrieved total rates count: {} for modId: {}", totalRates, modId);
          return totalRates;
        }
      }
    } catch (SQLException e) {
      logger.error("Database error while getting total rates for modId: {}", modId, e);
      throw e;
    } finally {
      // Clean up MDC
      MDC.remove("modId");
      MDC.remove("dbOperation");
    }
  }

  public long getRates(final long rate, final long modId) throws SQLException {
    // Add request-specific context to MDC
    MDC.put("modId", String.valueOf(modId));
    MDC.put("rateValue", String.valueOf(rate));
    MDC.put("dbOperation", "getRates");
    
    String sql = "SELECT COUNT(*) FROM rates WHERE rate = ? AND mod_id = ?";
    
    try {
      logger.debug("Executing getRates query for rate: {}, modId: {}", rate, modId);
      
      try (Connection conn = dataSource.getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setLong(1, rate);
        stmt.setLong(2, modId);
        
        try (ResultSet rs = stmt.executeQuery()) {
          long rateCount = rs.next() ? rs.getLong(1) : 0;
          logger.debug("Retrieved rate count: {} for rate: {}, modId: {}", rateCount, rate, modId);
          return rateCount;
        }
      }
    } catch (SQLException e) {
      logger.error("Database error while getting rates for rate: {}, modId: {}", rate, modId, e);
      throw e;
    } finally {
      // Clean up MDC
      MDC.remove("modId");
      MDC.remove("rateValue");
      MDC.remove("dbOperation");
    }
  }
}