package com.esclient.ratingservice.handler;

import com.esclient.ratingservice.constants.RatingConstants;
import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.service.RatingService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import rating.Rating;
import rating.RatingServiceGrpc;

@GrpcService
@SuppressWarnings({"checkstyle:RedundantModifier", "checkstyle:FinalParameters"})
public final class Handler extends RatingServiceGrpc.RatingServiceImplBase {
  private final RatingService service;

  public Handler(final RatingService service) {
    this.service = service;
  }

  @Override
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public void rateMod(
      final Rating.RateModRequest request,
      final StreamObserver<Rating.RateModResponse> responseObserver) {
    try {
      // Extract request data
      long modId = request.getModId();
      long authorId = request.getAuthorId();
      Rating.Rate rateEnum = request.getRate();

      validateRateModRequest(modId, authorId);

      int rateValue = convertRateEnumToInteger(rateEnum);

      // Call service
      int rateId = service.rateMod(modId, authorId, rateValue);

      // Build response
      Rating.RateModResponse response =
          Rating.RateModResponse.newBuilder().setRateId(rateId).build();

      // Send response
      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (IllegalArgumentException e) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid request parameters: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (IllegalStateException e) {
      responseObserver.onError(
          Status.FAILED_PRECONDITION
              .withDescription("Service state error: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Unexpected error occurred")
              .withCause(e)
              .asRuntimeException());
    }
  }

  @Override
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public void getRates(
      final Rating.GetRatesRequest request,
      final StreamObserver<Rating.GetRatesResponse> responseObserver) {
    try {
      long modId = request.getModId();
      RatingData ratingData = service.getRatings(modId);

      // Build response
      Rating.GetRatesResponse response =
          Rating.GetRatesResponse.newBuilder()
              .setRatesTotal(ratingData.getTotalRates())
              .setRate1(ratingData.getRate1Count())
              .setRate2(ratingData.getRate2Count())
              .setRate3(ratingData.getRate3Count())
              .setRate4(ratingData.getRate4Count())
              .setRate5(ratingData.getRate5Count())
              .build();

      // Send response
      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (IllegalArgumentException e) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid request parameters: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (IllegalStateException e) {
      responseObserver.onError(
          Status.FAILED_PRECONDITION
              .withDescription("Service state error: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (StatusRuntimeException e) {
      responseObserver.onError(e);
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Unexpected error occurred")
              .withCause(e)
              .asRuntimeException());
    }
  }

  private void validateRateModRequest(final long modId, final long authorId) {
    if (modId <= 0) {
      IllegalArgumentException e = new IllegalArgumentException("modId must be positive");
      service.logError(e);
      throw e;
    }
    if (authorId <= 0) {
      IllegalArgumentException e = new IllegalArgumentException("authorId must be positive");
      service.logError(e);
      throw e;
    }
  }

  private int convertRateEnumToInteger(final Rating.Rate rate) {
    switch (rate) {
      case RATE_1:
        return RatingConstants.RATE_1;
      case RATE_2:
        return RatingConstants.RATE_2;
      case RATE_3:
        return RatingConstants.RATE_3;
      case RATE_4:
        return RatingConstants.RATE_4;
      case RATE_5:
        return RatingConstants.RATE_5;
      case RATE_UNSPECIFIED:
      default:
        IllegalArgumentException e = new IllegalArgumentException("Invalid rate: " + rate);
        service.logError(e);
        throw e;
    }
  }
}
