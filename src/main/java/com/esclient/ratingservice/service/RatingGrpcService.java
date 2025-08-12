package com.esclient.ratingservice.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import rating.RatingServiceGrpc;
import rating.Rating;

@GrpcService
public class RatingGrpcService extends RatingServiceGrpc.RatingServiceImplBase {

    @Override
    public void rateMod(Rating.RateModRequest request, StreamObserver<Rating.RateModResponse> responseObserver) {
        try {
            // Extract request data
            long modId = request.getModId();
            long authorId = request.getAuthorId();
            int rating = request.getRate();
            
            // Your business logic here
            // TODO: Save rating to database
            // TODO: Validate rating value
            // TODO: Check if author already rated this mod
            
            
            // Build response
            Rating.RateModResponse response = Rating.RateModResponse.newBuilder()
                .setRateId(1337)
                .build();
            
            // Send response
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getRates(Rating.GetRatesRequest request, StreamObserver<Rating.GetRatesResponse> responseObserver) {
        try {
            long modId = request.getModId();
            
            // Your business logic here
            // TODO: Query database for ratings statistics
            // TODO: Calculate likes, dislikes, total
            
            // For now, return mock data
            long totalRates = 100L;
            long likes = 75L;
            long dislikes = 25L;
            
            // Build response
            Rating.GetRatesResponse response = Rating.GetRatesResponse.newBuilder()
                .setRatesTotal(totalRates)
                .setLikes(likes)
                .setDislikes(dislikes)
                .build();
            
            // Send response
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}