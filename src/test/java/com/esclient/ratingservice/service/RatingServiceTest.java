package com.esclient.ratingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.repository.RatingRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

  @Mock private RatingRepository mockRepository;

  private RatingService service;

  @BeforeEach
  void setUp() {
    service = new RatingService(mockRepository);
  }

  @Test
  void testRateModSuccess() throws SQLException {
    when(mockRepository.addRate(1L, 2L, 5)).thenReturn(1L);

    int result = service.rateMod(1L, 2L, 5);

    assertEquals(1, result);
  }

  @ParameterizedTest
  @MethodSource("rateModSQLErrorProvider")
  void testRateModWithSQLErrors(
      final String sqlState, final String errorMessage, final String expectedMessagePart)
      throws SQLException {
    SQLException sqlException = new SQLException(errorMessage, sqlState);
    when(mockRepository.addRate(anyLong(), anyLong(), anyInt())).thenThrow(sqlException);

    RatingServiceException exception =
        assertThrows(RatingServiceException.class, () -> service.rateMod(1L, 2L, 5));

    assertTrue(exception.getMessage().contains(expectedMessagePart));
    assertEquals(sqlException, exception.getCause());
  }

  private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments>
      rateModSQLErrorProvider() {
    return java.util.stream.Stream.of(
        org.junit.jupiter.params.provider.Arguments.of(
            "23505", "Constraint violation", "Database constraint violation"),
        org.junit.jupiter.params.provider.Arguments.of(
            "08001", "Connection failed", "Database connection error"),
        org.junit.jupiter.params.provider.Arguments.of(
            "99999", "Generic error", "Database error occurred"));
  }

  @Test
  void testRateModWithNullSQLState() throws SQLException {
    SQLException sqlException = new SQLException("Error without state");
    when(mockRepository.addRate(anyLong(), anyLong(), anyInt())).thenThrow(sqlException);

    RatingServiceException exception =
        assertThrows(RatingServiceException.class, () -> service.rateMod(1L, 2L, 5));

    assertTrue(exception.getMessage().contains("Database error occurred"));
    assertEquals(sqlException, exception.getCause());
  }

  @Test
  void testGetRatingsSuccess() throws SQLException {
    when(mockRepository.getTotalRates(1L)).thenReturn(10L);
    when(mockRepository.getRates(1, 1L)).thenReturn(2L);
    when(mockRepository.getRates(2, 1L)).thenReturn(1L);
    when(mockRepository.getRates(3, 1L)).thenReturn(3L);
    when(mockRepository.getRates(4, 1L)).thenReturn(2L);
    when(mockRepository.getRates(5, 1L)).thenReturn(2L);

    RatingData result = service.getRatings(1L);

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
      final String sqlState, final String errorMessage, final String expectedMessagePart)
      throws SQLException {
    SQLException sqlException = new SQLException(errorMessage, sqlState);
    when(mockRepository.getTotalRates(anyLong())).thenThrow(sqlException);

    RatingServiceException exception =
        assertThrows(RatingServiceException.class, () -> service.getRatings(1L));

    assertTrue(exception.getMessage().contains(expectedMessagePart));
    assertEquals(sqlException, exception.getCause());
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
  void testGetRatingsWithNullSQLState() throws SQLException {
    SQLException sqlException = new SQLException("Error without state");
    when(mockRepository.getTotalRates(anyLong())).thenThrow(sqlException);

    RatingServiceException exception =
        assertThrows(RatingServiceException.class, () -> service.getRatings(1L));

    assertTrue(exception.getMessage().contains("Database error occurred"));
    assertEquals(sqlException, exception.getCause());
  }

  @Test
  void testToString() {
    String result = service.toString();
    assertTrue(result.contains("RatingService"));
    assertTrue(result.contains("repository"));
  }

}
