package com.esclient.ratingservice.mapper;

import com.esclient.ratingservice.proto.Rating.RateModRequest;
import com.esclient.ratingservice.proto.Rating.RateModResponse;
import com.esclient.ratingservice.model.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {
    
    public Rating toRating(RateModRequest request) {
        return Rating.builder()
            .userId(String.valueOf(request.getAuthorId())) // Convert long to String
            .itemId(String.valueOf(request.getModId()))    // Map mod_id to item_id  
            .rating((double) request.getRating())          // Convert int32 to Double
            .build();
    }
    
    public RateModResponse toResponse(Rating savedRate) {
        return RateModResponse.newBuilder()
            .setRateId(savedRate.getId())
            .build();
    }
}