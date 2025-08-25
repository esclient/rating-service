package com.esclient.ratingservice.handler;

import com.esclient.ratingservice.model.RatingData;
import com.esclient.ratingservice.service.RatingService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import rating.Rating;
import rating.RatingServiceGrpc;

@GrpcService
@SuppressWarnings({"checkstyle:RedundantModifier", "checkstyle:FinalParameters"})
public final class Handler extends RatingServiceGrpc.RatingServiceImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
  private final RatingService service;

  public Handler(final RatingService service) {
    this.service = service;
  }

  @Override
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public void rateMod(
      final Rating.RateModRequest request,
      final StreamObserver<Rating.RateModResponse> responseObserver) {

    long modId = request.getModId();
    long authorId = request.getAuthorId();
    int rating = request.getRate();

    try (MDC.MDCCloseable methodCtx = MDC.putCloseable("grpcMethod", "rateMod");
        MDC.MDCCloseable modCtx = MDC.putCloseable("modId", String.valueOf(modId));
        MDC.MDCCloseable authorCtx = MDC.putCloseable("authorId", String.valueOf(authorId))) {

      LOGGER.info(
          "Received rateMod request - modId: {}, authorId: {}, rating: {}",
          modId,
          authorId,
          rating);

      int rateId = service.rateMod(modId, authorId, rating);

      Rating.RateModResponse response =
          Rating.RateModResponse.newBuilder().setRateId(rateId).build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

      LOGGER.info(
          "Successfully processed rateMod request - modId: {}, authorId: {}, rateId: {}",
          modId,
          authorId,
          rateId);

    } catch (IllegalArgumentException e) {
      LOGGER.warn(
          "Invalid argument in rateMod request - modId: {}, authorId: {}, rating: {} - Error: {}",
          modId,
          authorId,
          rating,
          e.getMessage());
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid request parameters: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (IllegalStateException e) {
      LOGGER.error(
          "Service state error in rateMod - modId: {}, authorId: {} - Error: {}",
          modId,
          authorId,
          e.getMessage(),
          e);
      responseObserver.onError(
          Status.FAILED_PRECONDITION
              .withDescription("Service state error: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (StatusRuntimeException e) {
      LOGGER.error(
          "gRPC status error in rateMod - modId: {}, authorId: {} - Status: {}",
          modId,
          authorId,
          e.getStatus(),
          e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOGGER.error("Unexpected error in rateMod - modId: {}, authorId: {}", modId, authorId, e);
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

    long modId = request.getModId();

    try (MDC.MDCCloseable methodCtx = MDC.putCloseable("grpcMethod", "getRates");
        MDC.MDCCloseable modCtx = MDC.putCloseable("modId", String.valueOf(modId))) {

      LOGGER.info("Received getRates request - modId: {}", modId);

      RatingData ratingData = service.getRatings(modId);

      Rating.GetRatesResponse response =
          Rating.GetRatesResponse.newBuilder()
              .setRatesTotal(ratingData.getTotalRates())
              .setLikes(ratingData.getLikes())
              .setDislikes(ratingData.getDislikes())
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

      LOGGER.info(
          "Successfully processed getRates request - modId: {}, total: {}, likes: {}, dislikes: {}",
          modId,
          ratingData.getTotalRates(),
          ratingData.getLikes(),
          ratingData.getDislikes());

    } catch (IllegalArgumentException e) {
      LOGGER.warn(
          "Invalid argument in getRates request - modId: {} - Error: {}", modId, e.getMessage());
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid request parameters: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (IllegalStateException e) {
      LOGGER.error(
          "Service state error in getRates - modId: {} - Error: {}", modId, e.getMessage(), e);
      responseObserver.onError(
          Status.FAILED_PRECONDITION
              .withDescription("Service state error: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    } catch (StatusRuntimeException e) {
      LOGGER.error(
          "gRPC status error in getRates - modId: {} - Status: {}", modId, e.getStatus(), e);
      responseObserver.onError(e);
    } catch (Exception e) {
      LOGGER.error("Unexpected error in getRates - modId: {}", modId, e);
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Unexpected error occurred")
              .withCause(e)
              .asRuntimeException());
    }
  }
}
