package com.esclient.ratingservice.handler;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import rating.RatingServiceGrpc;
import rating.Rating;
import com.esclient.ratingservice.service.RatingService;

@GrpcService
public class Handler extends RatingServiceGrpc.RatingServiceImplBase {
    private final RatingService service;  // Changed from "Service" to "RatingService"
   
    public Handler(RatingService service) {  // Changed from "Service" to "RatingService"
        this.service = service;
    }
   
    @Override
    public void rateMod(Rating.RateModRequest request, StreamObserver<Rating.RateModResponse> responseObserver) {
        try {
            // Extract request data
            long modId = request.getModId();
            long authorId = request.getAuthorId();
            int rating = request.getRate();
           
            // Use the correct variable names that you declared above
            int rateId = service.rateMod(modId, authorId, rating);
           
            // Build response
            Rating.RateModResponse response = Rating.RateModResponse.newBuilder()
                .setRateId(rateId)  // Use the actual returned rateId instead of hardcoded 1337
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