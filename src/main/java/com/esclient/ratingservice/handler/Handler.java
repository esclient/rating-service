package com.esclient.ratingservice.handler;

import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.service.RatingService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import rating.Rating;
import rating.RatingServiceGrpc;

@GrpcService
public class Handler extends RatingServiceGrpc.RatingServiceImplBase {
  private final RatingService service;

  public Handler(RatingService service) {
    this.service = service;
  }

  @Override
  public void rateMod(
      Rating.RateModRequest request, StreamObserver<Rating.RateModResponse> responseObserver) {
    try {
      // Extract request data
      long modId = request.getModId();
      long authorId = request.getAuthorId();
      int rating = request.getRate();

      int rateId = service.rateMod(modId, authorId, rating);

      // Build response
      Rating.RateModResponse response =
          Rating.RateModResponse.newBuilder()
              .setRateId(rateId) // Use the actual returned rateId instead of hardcoded 1337
              .build();

      // Send response
      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getRates(
      Rating.GetRatesRequest request, StreamObserver<Rating.GetRatesResponse> responseObserver) {
    try {
      long modId = request.getModId();

      RatingData ratingData = service.getRatings(modId);

      // Build response
      Rating.GetRatesResponse response =
          Rating.GetRatesResponse.newBuilder()
              .setRatesTotal(ratingData.getTotalRates())
              .setLikes(ratingData.getLikes())
              .setDislikes(ratingData.getDislikes())
              .build();

      // Send response
      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }
}
