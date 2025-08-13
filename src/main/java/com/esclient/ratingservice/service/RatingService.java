package com.esclient.ratingservice.service;

import com.esclient.ratingservice.repository.RatingRepository;
import com.esclient.ratingservice.model.RatingData;
import org.springframework.stereotype.Service;
import java.sql.SQLException;

@Service
public final class RatingService {
    private final RatingRepository repository;
    
    public RatingService(RatingRepository repository) {
        this.repository = repository;
    }
    
    public int rateMod(long mod_id, long author_id, int rate) {
        try {
            return (int) repository.addRate(mod_id, author_id, rate);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add rating", e);
        }
    }
    
    public RatingData getRatings(long modId) {
        try {
            long totalRates = repository.getTotalRates(modId);
            long dislikes = repository.getRates(0, modId);
            long likes = repository.getRates(1, modId);
            
            return new RatingData(totalRates, dislikes, likes);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get rating", e);
        }
    }
}