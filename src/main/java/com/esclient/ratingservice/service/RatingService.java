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

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingService.class);
    private final RatingRepository repository;

    // MDC key constants
    private static final String MDC_GRPC_METHOD = "grpc_method";
    private static final String MDC_MOD_ID = "mod_id";
    private static final String MDC_AUTHOR_ID = "author_id";

    public RatingService(final RatingRepository repository) {
        this.repository = repository;
    }

    public int rateMod(final long modId, final long authorId, final int rate) {
        MDC.put(MDC_GRPC_METHOD, "rate_mod");
        MDC.put(MDC_MOD_ID, String.valueOf(modId));
        MDC.put(MDC_AUTHOR_ID, String.valueOf(authorId));

        try {
            // --- INPUT VALIDATION ---
            if (modId <= 0) {
                LOGGER.error("Invalid modId: {}", modId);
                throw new IllegalArgumentException("modId must be positive");
            }
            if (authorId <= 0) {
                LOGGER.error("Invalid authorId: {}", authorId);
                throw new IllegalArgumentException("authorId must be positive");
            }
            if (rate < 0 || rate > 1) { 
                LOGGER.error("Invalid rating: {}", rate);
                throw new IllegalArgumentException("Rate must be 0 or 1");
            }

            return (int) repository.addRate(modId, authorId, rate);

        } catch (SQLException e) {
            LOGGER.error(
                    "Failed to add rating for mod {} by author {} with rate {}", modId, authorId, rate, e);
            throw new RatingServiceException("Failed to add rating", e);
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
            long dislikes = repository.getRates(0, modId);
            long likes = repository.getRates(1, modId);

            return new RatingData(totalRates, dislikes, likes);

        } catch (SQLException e) {
            LOGGER.error("Failed to get ratings for mod {}", modId, e);
            throw new RatingServiceException("Failed to get rating", e);
        } finally {
            MDC.remove(MDC_GRPC_METHOD);
            MDC.remove(MDC_MOD_ID);
        }
    }
}
