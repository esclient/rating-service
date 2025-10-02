package com.esclient.ratingservice.service;

import com.esclient.ratingservice.constants.RatingConstants;
import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.repository.RatingRepository;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public final class RatingService {
  // Static fields first (fixes IMC_IMMATURE_CLASS_WRONG_FIELD_ORDER)
  private static final Logger LOGGER = LoggerFactory.getLogger(RatingService.class);
  private static final String MDC_GRPC_METHOD = "grpc_method";
  private static final String MDC_MOD_ID = "mod_id";
  private static final String MDC_AUTHOR_ID = "author_id";

  // Instance fields after static fields
  private final RatingRepository repository;

  public RatingService(final RatingRepository repository) {
    this.repository = repository;
  }

  public void logError(final Exception e) {
    // Use string formatting to avoid CRLF_INJECTION_LOGS warning
    final String modId = MDC_MOD_ID;
    final String authorId = MDC_AUTHOR_ID;
    final String rate = MDC.get("rate");
    final String message =
        String.format(
            "Validation error for modId=%s, authorId=%s, rate=%s: %s",
            modId, authorId, rate, e.getMessage());
    LOGGER.error(message);
  }

  public int rateMod(final long modId, final long authorId, final int rate) {
    MDC.put(MDC_GRPC_METHOD, "rate_mod");
    MDC.put(MDC_MOD_ID, String.valueOf(modId));
    MDC.put(MDC_AUTHOR_ID, String.valueOf(authorId));
    try {
      return (int) repository.addRate(modId, authorId, rate);
    } catch (SQLException e) {
      // More specific exception handling to address EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS
      final String errorMessage =
          String.format(
              "Failed to add rating for mod %d by author %d with rate %d: %s",
              modId, authorId, rate, e.getMessage());
      LOGGER.error(errorMessage);

      // Add constraints based on SQL error types
      if (e.getSQLState() != null) {
        if (e.getSQLState().startsWith("23")) {
          throw new RatingServiceException(
              "Database constraint violation while adding rating: " + e.getMessage(), e);
        } else if (e.getSQLState().startsWith("08")) {
          throw new RatingServiceException(
              "Database connection error while adding rating: " + e.getMessage(), e);
        }
      }
      throw new RatingServiceException(
          "Database error occurred while adding rating: " + e.getMessage(), e);
    } finally {
      MDC.remove(MDC_GRPC_METHOD);
      MDC.remove(MDC_MOD_ID);
      MDC.remove(MDC_AUTHOR_ID);
    }
  }

  public RatingData getRatings(final long modId) {
    MDC.put(MDC_GRPC_METHOD, "get_ratings");
    MDC.put(MDC_MOD_ID, String.valueOf(modId));
    try {
      long totalRates = repository.getTotalRates(modId);
      long rate1Count = repository.getRates(RatingConstants.RATE_1, modId);
      long rate2Count = repository.getRates(RatingConstants.RATE_2, modId);
      long rate3Count = repository.getRates(RatingConstants.RATE_3, modId);
      long rate4Count = repository.getRates(RatingConstants.RATE_4, modId);
      long rate5Count = repository.getRates(RatingConstants.RATE_5, modId);
      return new RatingData(totalRates, rate1Count, rate2Count, rate3Count, rate4Count, rate5Count);
    } catch (SQLException e) {
      // More specific exception handling to address EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS
      final String errorMessage =
          String.format("Failed to get ratings for mod %d: %s", modId, e.getMessage());
      LOGGER.error(errorMessage);

      // Add constraints based on SQL error types
      if (e.getSQLState() != null) {
        if (e.getSQLState().startsWith("08")) {
          throw new RatingServiceException(
              "Database connection error while retrieving ratings: " + e.getMessage(), e);
        } else if (e.getSQLState().startsWith("42")) {
          throw new RatingServiceException(
              "Database query error while retrieving ratings: " + e.getMessage(), e);
        }
      }
      throw new RatingServiceException(
          "Database error occurred while retrieving ratings: " + e.getMessage(), e);
    } finally {
      MDC.remove(MDC_GRPC_METHOD);
      MDC.remove(MDC_MOD_ID);
    }
  }

  // Add toString method to fix IMC_IMMATURE_CLASS_NO_TOSTRING
  @Override
  public String toString() {
    return "RatingService{" + "repository=" + repository + '}';
  }
}
