package com.esclient.ratingservice.service;
import com.esclient.ratingservice.repository.RatingRepository;
import org.springframework.stereotype.Service;
import java.sql.SQLException;

@Service
public final class RatingService {
    private final RatingRepository repository;  // Changed from "Repository" to "RatingRepository"
   
    public RatingService(RatingRepository repository) {  // Changed parameter type to "RatingRepository"
        this.repository = repository;
    }
   
    public int rateMod(long mod_id, long author_id, int rate) {
        try {
            return (int) repository.addRate(mod_id, author_id, rate);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add rating", e);
        }
    }
}