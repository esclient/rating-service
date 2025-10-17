package ratingservice.handler;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import rating.Rating;
import rating.RatingServiceGrpc;
import ratingservice.constants.Constants;
import ratingservice.service.Service;

@SuppressWarnings("checkstyle:RedundantModifier")
public final class Handler extends RatingServiceGrpc.RatingServiceImplBase {

  private final Service service;

  public Handler(final Service service) {
    this.service = service;
  }

  @Override
  public void rateMod(
      final Rating.RateModRequest request,
      final StreamObserver<Rating.RateModResponse> responseObserver) {
    long modId = request.getModId();
    long authorId = request.getAuthorId();
    if (modId <= 0) {
      respondWithStatus(
          responseObserver,
          Status.INVALID_ARGUMENT,
          "Invalid request parameters: modId must be positive",
          new IllegalArgumentException("modId must be positive"));
      return;
    }
    if (authorId <= 0) {
      respondWithStatus(
          responseObserver,
          Status.INVALID_ARGUMENT,
          "Invalid request parameters: authorId must be positive",
          new IllegalArgumentException("authorId must be positive"));
      return;
    }
    int rateValue;
    try {
      rateValue = convertRateEnumToInteger(request.getRate());
    } catch (IllegalArgumentException e) {
      respondWithStatus(
          responseObserver,
          Status.INVALID_ARGUMENT,
          "Invalid request parameters: " + e.getMessage(),
          e);
      return;
    }

    service
        .rateMod(modId, authorId, rateValue)
        .whenComplete(
            (rateId, throwable) -> {
              if (throwable != null) {
                handleError(
                    "rateMod",
                    throwable,
                    responseObserver,
                    "Unexpected error occurred while processing rateMod request");
                return;
              }
              Rating.RateModResponse response =
                  Rating.RateModResponse.newBuilder().setRateId(rateId).build();
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            });
  }

  @Override
  public void getRates(
      final Rating.GetRatesRequest request,
      final StreamObserver<Rating.GetRatesResponse> responseObserver) {
    long modId = request.getModId();
    if (modId <= 0) {
      respondWithStatus(
          responseObserver,
          Status.INVALID_ARGUMENT,
          "Invalid request parameters: modId must be positive",
          new IllegalArgumentException("modId must be positive"));
      return;
    }

    service
        .getRatings(modId)
        .whenComplete(
            (data, throwable) -> {
              if (throwable != null) {
                handleError(
                    "getRates",
                    throwable,
                    responseObserver,
                    "Unexpected error occurred while processing getRates request");
                return;
              }
              Rating.GetRatesResponse response =
                  Rating.GetRatesResponse.newBuilder()
                      .setRatesTotal(data.getTotalRates())
                      .setRate1(data.getRate1Count())
                      .setRate2(data.getRate2Count())
                      .setRate3(data.getRate3Count())
                      .setRate4(data.getRate4Count())
                      .setRate5(data.getRate5Count())
                      .build();
              responseObserver.onNext(response);
              responseObserver.onCompleted();
            });
  }

  private void handleError(
      final String operation,
      final Throwable throwable,
      final StreamObserver<?> responseObserver,
      final String defaultMessage) {
    Throwable cause = unwrap(throwable);
    if (cause instanceof StatusRuntimeException) {
      responseObserver.onError(cause);
      return;
    }
    if (cause instanceof IllegalArgumentException) {
      respondWithStatus(
          responseObserver,
          Status.INVALID_ARGUMENT,
          "Invalid request parameters: " + cause.getMessage(),
          cause);
      return;
    }
    if (cause instanceof IllegalStateException) {
      respondWithStatus(
          responseObserver,
          Status.FAILED_PRECONDITION,
          "Service state error: " + cause.getMessage(),
          cause);
      return;
    }
    respondWithStatus(responseObserver, Status.INTERNAL, defaultMessage, cause);
  }

  private Throwable unwrap(final Throwable throwable) {
    if (throwable instanceof CompletionException || throwable instanceof ExecutionException) {
      Throwable cause = throwable.getCause();
      if (cause != null) {
        return unwrap(cause);
      }
    }
    return throwable;
  }

  private <T> void respondWithStatus(
      final StreamObserver<T> responseObserver,
      final Status status,
      final String message,
      final Throwable cause) {
    responseObserver.onError(status.withDescription(message).withCause(cause).asRuntimeException());
  }

  private int convertRateEnumToInteger(final Rating.Rate rate) {
    switch (rate) {
      case RATE_1:
        return Constants.RATE_1;
      case RATE_2:
        return Constants.RATE_2;
      case RATE_3:
        return Constants.RATE_3;
      case RATE_4:
        return Constants.RATE_4;
      case RATE_5:
        return Constants.RATE_5;
      case RATE_UNSPECIFIED:
      default:
        throw new IllegalArgumentException("Invalid rate: " + rate);
    }
  }
}
