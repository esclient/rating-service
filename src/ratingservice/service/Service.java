package ratingservice.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ratingservice.constants.Constants;
import ratingservice.model.Data;
import ratingservice.repository.Repository;

public final class Service {

  private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
  private static final String MDC_GRPC_METHOD = "grpc_method";
  private static final String MDC_MOD_ID = "mod_id";
  private static final String MDC_AUTHOR_ID = "author_id";
  private static final String MDC_RATE = "rate";

  private final Repository repository;
  private final Executor executor;

  public Service(final Repository repository, final Executor executor) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.executor = Objects.requireNonNull(executor, "executor");
  }

  public CompletionStage<Integer> rateMod(
      final long modId, final long authorId, final int rate) {
    Map<String, String> context = buildContext("rate_mod", modId, authorId, rate);
    return runAsync(
        context,
        () -> {
          validateIdentifiers(modId, authorId);
          validateRate(rate);
          try {
            long generatedId = repository.addRate(modId, authorId, rate);
            return Math.toIntExact(generatedId);
          } catch (SQLException e) {
            throw translateRateSqlException(modId, authorId, rate, e);
          } catch (ArithmeticException e) {
            LOGGER.error(
                "Generated identifier exceeds 32-bit range for mod {} by author {} with rate {}",
                modId,
                authorId,
                rate,
                e);
            throw new IllegalStateException("Rate identifier exceeds 32-bit range", e);
          }
        });
  }

  public CompletionStage<Data> getRatings(final long modId) {
    Map<String, String> context = buildContext("get_ratings", modId, null, null);
    return runAsync(
        context,
        () -> {
          validateModId(modId);
          try {
            long totalRates = repository.getTotalRates(modId);
            long rate1Count = repository.getRates(Constants.RATE_1, modId);
            long rate2Count = repository.getRates(Constants.RATE_2, modId);
            long rate3Count = repository.getRates(Constants.RATE_3, modId);
            long rate4Count = repository.getRates(Constants.RATE_4, modId);
            long rate5Count = repository.getRates(Constants.RATE_5, modId);
            return new Data(
                totalRates, rate1Count, rate2Count, rate3Count, rate4Count, rate5Count);
          } catch (SQLException e) {
            throw translateReadSqlException(modId, e);
          }
        });
  }

  private <T> CompletionStage<T> runAsync(
      final Map<String, String> context, final Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(() -> withMdc(context, supplier), executor);
  }

  private <T> T withMdc(final Map<String, String> context, final Supplier<T> supplier) {
    Map<String, String> previous = MDC.getCopyOfContextMap();
    try {
      MDC.clear();
      context.forEach(MDC::put);
      return supplier.get();
    } finally {
      if (previous == null || previous.isEmpty()) {
        MDC.clear();
      } else {
        MDC.setContextMap(previous);
      }
    }
  }

  private Map<String, String> buildContext(
      final String method, final long modId, final Long authorId, final Integer rate) {
    Map<String, String> context = new HashMap<>();
    context.put(MDC_GRPC_METHOD, method);
    context.put(MDC_MOD_ID, Long.toString(modId));
    if (authorId != null) {
      context.put(MDC_AUTHOR_ID, Long.toString(authorId));
    }
    if (rate != null) {
      context.put(MDC_RATE, Integer.toString(rate));
    }
    return context;
  }

  private void validateIdentifiers(final long modId, final long authorId) {
    validateModId(modId);
    validateAuthorId(authorId);
  }

  private void validateModId(final long modId) {
    if (modId <= 0) {
      IllegalArgumentException exception = new IllegalArgumentException("modId must be positive");
      LOGGER.warn("Validation failed: {}", exception.getMessage());
      throw exception;
    }
  }

  private void validateAuthorId(final long authorId) {
    if (authorId <= 0) {
      IllegalArgumentException exception =
          new IllegalArgumentException("authorId must be positive");
      LOGGER.warn("Validation failed: {}", exception.getMessage());
      throw exception;
    }
  }

  private void validateRate(final int rate) {
    if (rate < Constants.MIN_RATING || rate > Constants.MAX_RATING) {
      IllegalArgumentException exception =
          new IllegalArgumentException(
              "rate must be between " + Constants.MIN_RATING + " and "
                  + Constants.MAX_RATING);
      LOGGER.warn("Validation failed: {}", exception.getMessage());
      throw exception;
    }
  }

  private ServiceException translateRateSqlException(
      final long modId, final long authorId, final int rate, final SQLException e) {
    String sqlState = e.getSQLState();
    String message;
    if (sqlState != null) {
      if (sqlState.startsWith("23")) {
        message =
            "Database constraint violation while adding rating: "
                + e.getMessage();
      } else if (sqlState.startsWith("08")) {
        message =
            "Database connection error while adding rating: "
                + e.getMessage();
      } else {
        message = "Database error occurred while adding rating: " + e.getMessage();
      }
    } else {
      message = "Database error occurred while adding rating: " + e.getMessage();
    }
    LOGGER.error(
        "Failed to add rating for mod {} by author {} with rate {}",
        modId,
        authorId,
        rate,
        e);
    return new ServiceException(message, e);
  }

  private ServiceException translateReadSqlException(
      final long modId, final SQLException e) {
    String sqlState = e.getSQLState();
    String message;
    if (sqlState != null) {
      if (sqlState.startsWith("08")) {
        message =
            "Database connection error while retrieving ratings: "
                + e.getMessage();
      } else if (sqlState.startsWith("42")) {
        message =
            "Database query error while retrieving ratings: "
                + e.getMessage();
      } else {
        message = "Database error occurred while retrieving ratings: " + e.getMessage();
      }
    } else {
      message = "Database error occurred while retrieving ratings: " + e.getMessage();
    }
    LOGGER.error("Failed to get ratings for mod {}", modId, e);
    return new ServiceException(message, e);
  }

  @Override
  public String toString() {
    return "Service{" + "repository=" + repository + '}';
  }
}

