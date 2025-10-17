package rating;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.9.1)",
    comments = "Source: rating.proto")
public final class RatingServiceGrpc {

  private RatingServiceGrpc() {}

  public static final String SERVICE_NAME = "rating.RatingService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getRateModMethod()} instead. 
  public static final io.grpc.MethodDescriptor<rating.Rating.RateModRequest,
      rating.Rating.RateModResponse> METHOD_RATE_MOD = getRateModMethod();

  private static volatile io.grpc.MethodDescriptor<rating.Rating.RateModRequest,
      rating.Rating.RateModResponse> getRateModMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<rating.Rating.RateModRequest,
      rating.Rating.RateModResponse> getRateModMethod() {
    io.grpc.MethodDescriptor<rating.Rating.RateModRequest, rating.Rating.RateModResponse> getRateModMethod;
    if ((getRateModMethod = RatingServiceGrpc.getRateModMethod) == null) {
      synchronized (RatingServiceGrpc.class) {
        if ((getRateModMethod = RatingServiceGrpc.getRateModMethod) == null) {
          RatingServiceGrpc.getRateModMethod = getRateModMethod = 
              io.grpc.MethodDescriptor.<rating.Rating.RateModRequest, rating.Rating.RateModResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "rating.RatingService", "RateMod"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rating.Rating.RateModRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rating.Rating.RateModResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new RatingServiceMethodDescriptorSupplier("RateMod"))
                  .build();
          }
        }
     }
     return getRateModMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetRatesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<rating.Rating.GetRatesRequest,
      rating.Rating.GetRatesResponse> METHOD_GET_RATES = getGetRatesMethod();

  private static volatile io.grpc.MethodDescriptor<rating.Rating.GetRatesRequest,
      rating.Rating.GetRatesResponse> getGetRatesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<rating.Rating.GetRatesRequest,
      rating.Rating.GetRatesResponse> getGetRatesMethod() {
    io.grpc.MethodDescriptor<rating.Rating.GetRatesRequest, rating.Rating.GetRatesResponse> getGetRatesMethod;
    if ((getGetRatesMethod = RatingServiceGrpc.getGetRatesMethod) == null) {
      synchronized (RatingServiceGrpc.class) {
        if ((getGetRatesMethod = RatingServiceGrpc.getGetRatesMethod) == null) {
          RatingServiceGrpc.getGetRatesMethod = getGetRatesMethod = 
              io.grpc.MethodDescriptor.<rating.Rating.GetRatesRequest, rating.Rating.GetRatesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "rating.RatingService", "GetRates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rating.Rating.GetRatesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rating.Rating.GetRatesResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new RatingServiceMethodDescriptorSupplier("GetRates"))
                  .build();
          }
        }
     }
     return getGetRatesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RatingServiceStub newStub(io.grpc.Channel channel) {
    return new RatingServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RatingServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new RatingServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RatingServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new RatingServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class RatingServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void rateMod(rating.Rating.RateModRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.RateModResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRateModMethod(), responseObserver);
    }

    /**
     */
    public void getRates(rating.Rating.GetRatesRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.GetRatesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetRatesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRateModMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                rating.Rating.RateModRequest,
                rating.Rating.RateModResponse>(
                  this, METHODID_RATE_MOD)))
          .addMethod(
            getGetRatesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                rating.Rating.GetRatesRequest,
                rating.Rating.GetRatesResponse>(
                  this, METHODID_GET_RATES)))
          .build();
    }
  }

  /**
   */
  public static final class RatingServiceStub extends io.grpc.stub.AbstractStub<RatingServiceStub> {
    private RatingServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RatingServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RatingServiceStub(channel, callOptions);
    }

    /**
     */
    public void rateMod(rating.Rating.RateModRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.RateModResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRateModMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRates(rating.Rating.GetRatesRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.GetRatesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetRatesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class RatingServiceBlockingStub extends io.grpc.stub.AbstractStub<RatingServiceBlockingStub> {
    private RatingServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RatingServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RatingServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public rating.Rating.RateModResponse rateMod(rating.Rating.RateModRequest request) {
      return blockingUnaryCall(
          getChannel(), getRateModMethod(), getCallOptions(), request);
    }

    /**
     */
    public rating.Rating.GetRatesResponse getRates(rating.Rating.GetRatesRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetRatesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class RatingServiceFutureStub extends io.grpc.stub.AbstractStub<RatingServiceFutureStub> {
    private RatingServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RatingServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RatingServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<rating.Rating.RateModResponse> rateMod(
        rating.Rating.RateModRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRateModMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<rating.Rating.GetRatesResponse> getRates(
        rating.Rating.GetRatesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetRatesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_RATE_MOD = 0;
  private static final int METHODID_GET_RATES = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RatingServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RatingServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RATE_MOD:
          serviceImpl.rateMod((rating.Rating.RateModRequest) request,
              (io.grpc.stub.StreamObserver<rating.Rating.RateModResponse>) responseObserver);
          break;
        case METHODID_GET_RATES:
          serviceImpl.getRates((rating.Rating.GetRatesRequest) request,
              (io.grpc.stub.StreamObserver<rating.Rating.GetRatesResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class RatingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RatingServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return rating.Rating.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("RatingService");
    }
  }

  private static final class RatingServiceFileDescriptorSupplier
      extends RatingServiceBaseDescriptorSupplier {
    RatingServiceFileDescriptorSupplier() {}
  }

  private static final class RatingServiceMethodDescriptorSupplier
      extends RatingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RatingServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RatingServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RatingServiceFileDescriptorSupplier())
              .addMethod(getRateModMethod())
              .addMethod(getGetRatesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
