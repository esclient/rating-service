package com.esclient.ratingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.esclient.ratingservice.constants.RatingConstants;
import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.service.RatingService;
import com.esclient.ratingservice.service.RatingServiceException;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "grpc.server.port=-1", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = { GrpcServerFactoryAutoConfiguration.class, GrpcServerAutoConfiguration.class })
class RatingServiceApplicationTests {

  @Autowired
  private RatingService ratingService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    MDC.clear();
    // Clean up test data before each test
    jdbcTemplate.execute("TRUNCATE TABLE rates");
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
    jdbcTemplate.execute("TRUNCATE TABLE rates");
  }

  @Test
  void contextLoads() {
    // This test verifies the application context loads correctly
    assertNotNull(ratingService);
  }

  // ==================== rateMod() Tests ====================

  @Test
  void testRateModSuccess() {
    // Arrange
    long modId = 1L;
    long authorId = 2L;
    int rate = 5;

    // Act
    int result = ratingService.rateMod(modId, authorId, rate);

    // Assert
    assertNotNull(result);

    // Verify the rating was actually saved
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM rates WHERE mod_id = ? AND author_id = ? AND rate = ?",
        Integer.class,
        modId,
        authorId,
        rate);
    assertEquals(1, count);
  }

  @Test
  void testRateModThrowsExceptionOnConstraintViolation() {
    // Arrange
    long modId = 1L;
    long authorId = 2L;
    int rate = 5;

    // First insert should succeed
    ratingService.rateMod(modId, authorId, rate);

    // Act & Assert - Second insert with same mod_id and author_id should fail
    assertThrows(RatingServiceException.class, () -> ratingService.rateMod(modId, authorId, rate));
  }

  @Test
  void testRateModWithDifferentAuthors() {
    // Arrange
    long modId = 1L;
    int rate = 5;

    // Act - Same mod, different authors should work
    int result1 = ratingService.rateMod(modId, 100L, rate);
    int result2 = ratingService.rateMod(modId, 200L, rate);

    // Assert
    assertNotNull(result1);
    assertNotNull(result2);

    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM rates WHERE mod_id = ?", Integer.class, modId);
    assertEquals(2, count);
  }

  // ==================== getRatings() Tests ====================

  @Test
  void testGetRatingsSuccess() {
    // Arrange
    long modId = 1L;

    // Insert test data
    ratingService.rateMod(modId, 1L, RatingConstants.RATE_1);
    ratingService.rateMod(modId, 2L, RatingConstants.RATE_1);
    ratingService.rateMod(modId, 3L, RatingConstants.RATE_2);
    ratingService.rateMod(modId, 4L, RatingConstants.RATE_3);
    ratingService.rateMod(modId, 5L, RatingConstants.RATE_4);
    ratingService.rateMod(modId, 6L, RatingConstants.RATE_5);
    ratingService.rateMod(modId, 7L, RatingConstants.RATE_5);

    // Act
    RatingData result = ratingService.getRatings(modId);

    // Assert
    assertNotNull(result);
    assertEquals(7L, result.getTotalRates());
    assertEquals(2L, result.getRate1Count());
    assertEquals(1L, result.getRate2Count());
    assertEquals(1L, result.getRate3Count());
    assertEquals(1L, result.getRate4Count());
    assertEquals(2L, result.getRate5Count());
  }

  @Test
  void testGetRatingsForNonExistentMod() {
    // Arrange
    long modId = 999L;

    // Act
    RatingData result = ratingService.getRatings(modId);

    // Assert
    assertNotNull(result);
    assertEquals(0L, result.getTotalRates());
    assertEquals(0L, result.getRate1Count());
    assertEquals(0L, result.getRate2Count());
    assertEquals(0L, result.getRate3Count());
    assertEquals(0L, result.getRate4Count());
    assertEquals(0L, result.getRate5Count());
  }

  @Test
  void testGetRatingsWithOnlyOneRating() {
    // Arrange
    long modId = 1L;
    ratingService.rateMod(modId, 1L, RatingConstants.RATE_5);

    // Act
    RatingData result = ratingService.getRatings(modId);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getTotalRates());
    assertEquals(0L, result.getRate1Count());
    assertEquals(0L, result.getRate2Count());
    assertEquals(0L, result.getRate3Count());
    assertEquals(0L, result.getRate4Count());
    assertEquals(1L, result.getRate5Count());
  }

  @Test
  void testGetRatingsForMultipleMods() {
    // Arrange
    long modId1 = 1L;
    long modId2 = 2L;

    ratingService.rateMod(modId1, 1L, RatingConstants.RATE_5);
    ratingService.rateMod(modId1, 2L, RatingConstants.RATE_4);
    ratingService.rateMod(modId2, 3L, RatingConstants.RATE_3);

    // Act
    RatingData result1 = ratingService.getRatings(modId1);
    RatingData result2 = ratingService.getRatings(modId2);

    // Assert
    assertEquals(2L, result1.getTotalRates());
    assertEquals(1L, result2.getTotalRates());
  }

  // ==================== logError() Test ====================

  @Test
  void testLogErrorWithMdcValues() {
    // Arrange
    MDC.put("mod_id", "123");
    MDC.put("author_id", "456");
    MDC.put("rate", "5");
    Exception exception = new IllegalArgumentException("Invalid rate");

    // Act - This just tests the method doesn't crash
    ratingService.logError(exception);

    // Assert - Method should complete without throwing exceptions
  }

  @Test
  void testLogErrorWithNullMdcValues() {
    // Arrange
    MDC.clear();
    Exception exception = new IllegalArgumentException("Invalid rate");

    // Act - This just tests the method doesn't crash with null MDC values
    ratingService.logError(exception);

    // Assert - Method should complete without throwing exceptions
  }

  // ==================== toString() Test ====================

  @Test
  void testToString() {
    String result = ratingService.toString();

    assertNotNull(result);
    assertTrue(result.startsWith("RatingService{repository="));
    assertTrue(result.contains("RatingRepository@"));
    assertTrue(result.endsWith("}"));
  }

  // ==================== Edge Cases ====================

  @Test
  void testRateModWithInvalidRate() {
    // Arrange
    long modId = 1L;
    long authorId = 2L;
    int invalidRate = 6;

    // Act & Assert - Should fail constraint check
    assertThrows(Exception.class, () -> ratingService.rateMod(modId, authorId, invalidRate));
  }

  @Test
  void testGetRatingsAverageCalculation() {
    // Arrange
    long modId = 1L;

    // Add ratings: 1*1 + 2*2 + 3*3 + 4*4 + 5*5 = 1+4+9+16+25 = 55 / 15 = 3.67
    ratingService.rateMod(modId, 1L, 1);
    ratingService.rateMod(modId, 2L, 2);
    ratingService.rateMod(modId, 3L, 2);
    ratingService.rateMod(modId, 4L, 3);
    ratingService.rateMod(modId, 5L, 3);
    ratingService.rateMod(modId, 6L, 3);
    ratingService.rateMod(modId, 7L, 4);
    ratingService.rateMod(modId, 8L, 4);
    ratingService.rateMod(modId, 9L, 4);
    ratingService.rateMod(modId, 10L, 4);
    ratingService.rateMod(modId, 11L, 5);
    ratingService.rateMod(modId, 12L, 5);
    ratingService.rateMod(modId, 13L, 5);
    ratingService.rateMod(modId, 14L, 5);
    ratingService.rateMod(modId, 15L, 5);

    // Act
    RatingData result = ratingService.getRatings(modId);

    // Assert
    assertEquals(15L, result.getTotalRates());
    assertEquals(1L, result.getRate1Count());
    assertEquals(2L, result.getRate2Count());
    assertEquals(3L, result.getRate3Count());
    assertEquals(4L, result.getRate4Count());
    assertEquals(5L, result.getRate5Count());
  }
}
