package ratingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ratingservice.model.Data;
import ratingservice.repository.Repository;

@ExtendWith(MockitoExtension.class)
class ServiceTest {

  @Mock private Repository mockRepository;

  private Service service;
  private ExecutorService executor;

  @BeforeEach
  void setUp() {
    executor = Executors.newSingleThreadExecutor();
    service = new Service(mockRepository, executor);
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    executor.shutdownNow();
    executor.awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  void testRateModSuccess() throws Exception {
    when(mockRepository.addRate(1L, 2L, 5)).thenReturn(1L);

    int result = await(service.rateMod(1L, 2L, 5));

    assertEquals(1, result);
  }

  @ParameterizedTest
  @MethodSource("rateModSQLErrorProvider")
  void testRateModWithSQLErrors(
      final String sqlState, final String errorMessage, final String expectedMessagePart)
      throws Exception {
    SQLException sqlException = new SQLException(errorMessage, sqlState);
    when(mockRepository.addRate(anyLong(), anyLong(), anyInt())).thenThrow(sqlException);

    ExecutionException exception =
        assertThrows(ExecutionException.class, () -> await(service.rateMod(1L, 2L, 5)));

    Throwable cause = exception.getCause();
    assertTrue(cause instanceof IllegalStateException);
    assertTrue(cause.getMessage().contains(expectedMessagePart));
    assertEquals(sqlException, cause.getCause());
  }

  private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments>
      rateModSQLErrorProvider() {
    return java.util.stream.Stream.of(
        org.junit.jupiter.params.provider.Arguments.of(
            "23505", "Constraint violation", "Database error occurred while adding rating"),
        org.junit.jupiter.params.provider.Arguments.of(
            "08001", "Connection failed", "Database error occurred while adding rating"),
        org.junit.jupiter.params.provider.Arguments.of(
            "99999", "Generic error", "Database error occurred while adding rating"));
  }

  @Test
  void testRateModWithNullSQLState() throws Exception {
    SQLException sqlException = new SQLException("Error without state");
    when(mockRepository.addRate(anyLong(), anyLong(), anyInt())).thenThrow(sqlException);

    ExecutionException exception =
        assertThrows(ExecutionException.class, () -> await(service.rateMod(1L, 2L, 5)));

    Throwable cause = exception.getCause();
    assertTrue(cause instanceof IllegalStateException);
    assertTrue(cause.getMessage().contains("Database error occurred"));
    assertEquals(sqlException, cause.getCause());
  }

  @Test
  void testRateModDetectsOverflow() throws Exception {
    when(mockRepository.addRate(1L, 2L, 5)).thenReturn((long) Integer.MAX_VALUE + 10);

    ExecutionException exception =
        assertThrows(ExecutionException.class, () -> await(service.rateMod(1L, 2L, 5)));

    assertTrue(exception.getCause() instanceof IllegalStateException);
  }

  @Test
  void testGetRatingsSuccess() throws Exception {
    when(mockRepository.getRatingSummary(1L)).thenReturn(new Data(10L, 2L, 1L, 3L, 2L, 2L));

    Data result = await(service.getRatings(1L));

    assertEquals(10L, result.getTotalRates());
    assertEquals(2L, result.getRate1Count());
    assertEquals(1L, result.getRate2Count());
    assertEquals(3L, result.getRate3Count());
    assertEquals(2L, result.getRate4Count());
    assertEquals(2L, result.getRate5Count());
  }

  @ParameterizedTest
  @MethodSource("getRatingsSQLErrorProvider")
  void testGetRatingsWithSQLErrors(
      final String sqlState, final String errorMessage, final String ignored) throws Exception {
    SQLException sqlException = new SQLException(errorMessage, sqlState);
    when(mockRepository.getRatingSummary(anyLong())).thenThrow(sqlException);

    ExecutionException exception =
        assertThrows(ExecutionException.class, () -> await(service.getRatings(1L)));

    Throwable cause = exception.getCause();
    assertTrue(cause instanceof RuntimeException);
    assertEquals(sqlException, cause.getCause());
  }

  private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments>
      getRatingsSQLErrorProvider() {
    return java.util.stream.Stream.of(
        org.junit.jupiter.params.provider.Arguments.of(
            "08006", "Connection lost", "Database connection error"),
        org.junit.jupiter.params.provider.Arguments.of(
            "42601", "Query syntax error", "Database query error"),
        org.junit.jupiter.params.provider.Arguments.of(
            "99999", "Generic error", "Database error occurred"));
  }

  @Test
  void testGetRatingsWithNullSQLState() throws Exception {
    SQLException sqlException = new SQLException("Error without state");
    when(mockRepository.getRatingSummary(anyLong())).thenThrow(sqlException);

    ExecutionException exception =
        assertThrows(ExecutionException.class, () -> await(service.getRatings(1L)));

    Throwable cause = exception.getCause();
    assertTrue(cause instanceof RuntimeException);
    assertEquals(sqlException, cause.getCause());
  }

  @Test
  void testToString() {
    String result = service.toString();
    assertTrue(result.contains("Service"));
    assertTrue(result.contains("repository"));
  }

  private <T> T await(final java.util.concurrent.CompletionStage<T> stage) throws Exception {
    return stage.toCompletableFuture().get(1, TimeUnit.SECONDS);
  }
}
