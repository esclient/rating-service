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

  public CompletionStage<Integer> rateMod(final long modId, final long authorId, final int rate) {
    Map<String, String> context = new HashMap<>();
    context.put(MDC_GRPC_METHOD, "rate_mod");
    context.put(MDC_MOD_ID, String.valueOf(modId));
    context.put(MDC_AUTHOR_ID, String.valueOf(authorId));
    context.put(MDC_RATE, String.valueOf(rate));
    return runAsync(
        context,
        () -> {
          try {
            long generatedId = repository.addRate(modId, authorId, rate);
            return Math.toIntExact(generatedId);
          } catch (SQLException e) {
            LOGGER.error(
                "Failed to add rating for mod {} by author {} with rate {}",
                modId,
                authorId,
                rate,
                e);
            throw new IllegalStateException(
                "Database error occurred while adding rating: " + e.getMessage(), e);
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
    Map<String, String> context = new HashMap<>();
    context.put(MDC_GRPC_METHOD, "get_ratings");
    context.put(MDC_MOD_ID, String.valueOf(modId));
    return runAsync(
        context,
        () -> {
          try {
            return repository.getRatingSummary(modId);
          } catch (SQLException e) {
            throw new RuntimeException(e);
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

  @Override
  public String toString() {
    return "Service{" + "repository=" + repository + '}';
  }
}
