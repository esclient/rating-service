package com.esclient.ratingservice.mapper;


import com.esclient.ratingservice.model.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {


    public Rating toRating(com.esclient.ratingservice.proto.RateModRequest request) {
        return Rating.builder()
                .userId(request.getAuthorId())
                .modId(request.getModId())
                .likes(request.getRating())
                .build();
    }

    public com.esclient.ratingservice.proto.RateModResponse toResponse(Rating savedRate) {
        return com.esclient.ratingservice.proto.RateModResponse.newBuilder()
                .setRateId(savedRate.getId())
                .build();
    }
}
