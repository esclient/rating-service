package com.esclient.ratingservice.mapper;

import rating.Rating.RateModRequest;
import rating.Rating.RateModResponse;
import com.esclient.ratingservice.model.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public Rating toRating(RateModRequest request) {
        return Rating.builder()
            .userId(request.getAuthorId())
            .modId(request.getModId())
            .likes(request.getRating())
            .build();
    }

    public RateModResponse toResponse(Rating savedRate) {
        return RateModResponse.newBuilder()
            .setRateId(savedRate.getId())
            .build();
    }
}
