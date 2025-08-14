package com.esclient.ratingservice.service;

import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.repository.RatingRepository;
import java.sql.SQLException;
import org.springframework.stereotype.Service;

@Service
public final class RatingService {
  private final RatingRepository repository;

  public RatingService(final RatingRepository repository) {
    this.repository = repository;
  }

  public int rateMod(final long modId, final long authorId, final int rate) {
    try {
      return (int) repository.addRate(modId, authorId, rate);
    } catch (SQLException e) {
      throw new RatingServiceException("Failed to add rating", e);
    }
  }

  public RatingData getRatings(final long modId) {
    try {
      long totalRates = repository.getTotalRates(modId);
      long dislikes = repository.getRates(0, modId);
      long likes = repository.getRates(1, modId);

      return new RatingData(totalRates, dislikes, likes);
    } catch (SQLException e) {
      throw new RatingServiceException("Failed to get rating", e);
    }
  }
}
