package com.esclient.ratingservice.service;

import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.repository.RatingRepository;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public final class RatingService {
  
  private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
  private final RatingRepository repository;

  public RatingService(final RatingRepository repository) {
    this.repository = repository;
  }

  public int rateMod(final long modId, final long authorId, final int rate) {
    // Add request-specific context to MDC
    MDC.put("modId", String.valueOf(modId));
    MDC.put("authorId", String.valueOf(authorId));
    MDC.put("operation", "rateMod");
    
    try {
      logger.info("Rating mod {} by author {} with rate: {}", modId, authorId, rate);
      
      int result = (int) repository.addRate(modId, authorId, rate);
      
      logger.info("Successfully rated mod {} by author {} with result: {}", modId, authorId, result);
      logger.debug("Rating operation completed - modId: {}, authorId: {}, rate: {}, result: {}", 
                   modId, authorId, rate, result);
      
      return result;
      
    } catch (SQLException e) {
      logger.error("Failed to add rating for mod {} by author {} with rate {}", 
                   modId, authorId, rate, e);
      throw new RatingServiceException("Failed to add rating", e);
    } finally {
      // Clean up MDC
      MDC.remove("modId");
      MDC.remove("authorId");
      MDC.remove("operation");
    }
  }

  public RatingData getRatings(final long modId) {
    // Add request-specific context to MDC
    MDC.put("modId", String.valueOf(modId));
    MDC.put("operation", "getRatings");
    
    try {
      logger.info("Retrieving ratings for mod: {}", modId);
      
      long totalRates = repository.getTotalRates(modId);
      long dislikes = repository.getRates(0, modId);
      long likes = repository.getRates(1, modId);
      
      RatingData result = new RatingData(totalRates, dislikes, likes);
      
      logger.info("Successfully retrieved ratings for mod {} - Total: {}, Likes: {}, Dislikes: {}", 
                  modId, totalRates, likes, dislikes);
      logger.debug("Rating data retrieved: {}", result);
      
      return result;
      
    } catch (SQLException e) {
      logger.error("Failed to get ratings for mod {}", modId, e);
      throw new RatingServiceException("Failed to get rating", e);
    } finally {
      // Clean up MDC
      MDC.remove("modId");
      MDC.remove("operation");
    }
  }
}