package ratingservice.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rating.Rating;
import ratingservice.model.Data;
import ratingservice.service.Service;

@ExtendWith(MockitoExtension.class)
final class HandlerTest {

  @Mock private Service ratingService;
  @Mock private StreamObserver<Rating.RateModResponse> rateModObserver;
  @Mock private StreamObserver<Rating.GetRatesResponse> getRatesObserver;

  private Handler handler;

  @BeforeEach
  void setUp() {
    handler = new Handler(ratingService);
  }

  @Test
  void rateMod_shouldEmitResponseAndComplete() {
    when(ratingService.rateMod(1L, 2L, 5)).thenReturn(CompletableFuture.completedFuture(42));

    Rating.RateModRequest request =
        Rating.RateModRequest.newBuilder()
            .setModId(1L)
            .setAuthorId(2L)
            .setRate(Rating.Rate.RATE_5)
            .build();

    handler.rateMod(request, rateModObserver);

    ArgumentCaptor<Rating.RateModResponse> responseCaptor =
        ArgumentCaptor.forClass(Rating.RateModResponse.class);
    verify(rateModObserver, timeout(200)).onNext(responseCaptor.capture());
    assertEquals(42, responseCaptor.getValue().getRateId());
    verify(rateModObserver, timeout(200)).onCompleted();
  }

  @Test
  void rateMod_shouldReturnInvalidArgumentForNegativeModId() {
    when(ratingService.rateMod(0L, 2L, 3))
        .thenReturn(
            CompletableFuture.failedFuture(new IllegalArgumentException("modId must be positive")));

    Rating.RateModRequest request =
        Rating.RateModRequest.newBuilder()
            .setModId(0L)
            .setAuthorId(2L)
            .setRate(Rating.Rate.RATE_3)
            .build();

    handler.rateMod(request, rateModObserver);

    ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
    verify(rateModObserver, timeout(200)).onError(errorCaptor.capture());
    Status status = Status.fromThrowable(errorCaptor.getValue());
    assertSame(Status.INVALID_ARGUMENT.getCode(), status.getCode());
    verify(rateModObserver, never()).onCompleted();
  }

  @Test
  void rateMod_shouldTranslateServiceIllegalState() {
    when(ratingService.rateMod(1L, 2L, 1))
        .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("state")));

    Rating.RateModRequest request =
        Rating.RateModRequest.newBuilder()
            .setModId(1L)
            .setAuthorId(2L)
            .setRate(Rating.Rate.RATE_1)
            .build();

    handler.rateMod(request, rateModObserver);

    ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
    verify(rateModObserver, timeout(200)).onError(errorCaptor.capture());
    Status status = Status.fromThrowable(errorCaptor.getValue());
    assertSame(Status.FAILED_PRECONDITION.getCode(), status.getCode());
  }

  @Test
  void rateMod_shouldTranslateUnexpectedErrors() {
    when(ratingService.rateMod(1L, 2L, 4))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

    Rating.RateModRequest request =
        Rating.RateModRequest.newBuilder()
            .setModId(1L)
            .setAuthorId(2L)
            .setRate(Rating.Rate.RATE_4)
            .build();

    handler.rateMod(request, rateModObserver);

    ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
    verify(rateModObserver, timeout(200)).onError(errorCaptor.capture());
    Status status = Status.fromThrowable(errorCaptor.getValue());
    assertSame(Status.INTERNAL.getCode(), status.getCode());
  }

  @Test
  void getRates_shouldEmitMappedResponse() {
    Data data = new Data(10, 1, 2, 3, 4, 5);
    when(ratingService.getRatings(7L)).thenReturn(CompletableFuture.completedFuture(data));

    Rating.GetRatesRequest request = Rating.GetRatesRequest.newBuilder().setModId(7L).build();

    handler.getRates(request, getRatesObserver);

    ArgumentCaptor<Rating.GetRatesResponse> responseCaptor =
        ArgumentCaptor.forClass(Rating.GetRatesResponse.class);
    verify(getRatesObserver, timeout(200)).onNext(responseCaptor.capture());
    Rating.GetRatesResponse response = responseCaptor.getValue();
    assertEquals(10, response.getRatesTotal());
    assertEquals(1, response.getRate1());
    assertEquals(2, response.getRate2());
    assertEquals(3, response.getRate3());
    assertEquals(4, response.getRate4());
    assertEquals(5, response.getRate5());
    verify(getRatesObserver, timeout(200)).onCompleted();
  }

  @Test
  void getRates_shouldTranslateServiceErrors() {
    when(ratingService.getRatings(9L))
        .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("bad")));

    Rating.GetRatesRequest request = Rating.GetRatesRequest.newBuilder().setModId(9L).build();

    handler.getRates(request, getRatesObserver);

    ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
    verify(getRatesObserver, timeout(200)).onError(errorCaptor.capture());
    Status status = Status.fromThrowable(errorCaptor.getValue());
    assertSame(Status.INVALID_ARGUMENT.getCode(), status.getCode());
  }
}
