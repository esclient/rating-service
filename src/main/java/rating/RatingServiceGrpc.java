package rating;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: rating.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RatingServiceGrpc {

  private RatingServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "rating.RatingService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<rating.Rating.RateModRequest,
      rating.Rating.RateModResponse> getRateModMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RateMod",
      requestType = rating.Rating.RateModRequest.class,
      responseType = rating.Rating.RateModResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<rating.Rating.RateModRequest,
      rating.Rating.RateModResponse> getRateModMethod() {
    io.grpc.MethodDescriptor<rating.Rating.RateModRequest, rating.Rating.RateModResponse> getRateModMethod;
    if ((getRateModMethod = RatingServiceGrpc.getRateModMethod) == null) {
      synchronized (RatingServiceGrpc.class) {
        if ((getRateModMethod = RatingServiceGrpc.getRateModMethod) == null) {
          RatingServiceGrpc.getRateModMethod = getRateModMethod =
              io.grpc.MethodDescriptor.<rating.Rating.RateModRequest, rating.Rating.RateModResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RateMod"))
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

  private static volatile io.grpc.MethodDescriptor<rating.Rating.GetRatesRequest,
      rating.Rating.GetRatesResponse> getGetRatesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRates",
      requestType = rating.Rating.GetRatesRequest.class,
      responseType = rating.Rating.GetRatesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<rating.Rating.GetRatesRequest,
      rating.Rating.GetRatesResponse> getGetRatesMethod() {
    io.grpc.MethodDescriptor<rating.Rating.GetRatesRequest, rating.Rating.GetRatesResponse> getGetRatesMethod;
    if ((getGetRatesMethod = RatingServiceGrpc.getGetRatesMethod) == null) {
      synchronized (RatingServiceGrpc.class) {
        if ((getGetRatesMethod = RatingServiceGrpc.getGetRatesMethod) == null) {
          RatingServiceGrpc.getGetRatesMethod = getGetRatesMethod =
              io.grpc.MethodDescriptor.<rating.Rating.GetRatesRequest, rating.Rating.GetRatesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetRates"))
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
    io.grpc.stub.AbstractStub.StubFactory<RatingServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RatingServiceStub>() {
        @java.lang.Override
        public RatingServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RatingServiceStub(channel, callOptions);
        }
      };
    return RatingServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RatingServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RatingServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RatingServiceBlockingStub>() {
        @java.lang.Override
        public RatingServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RatingServiceBlockingStub(channel, callOptions);
        }
      };
    return RatingServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RatingServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RatingServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RatingServiceFutureStub>() {
        @java.lang.Override
        public RatingServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RatingServiceFutureStub(channel, callOptions);
        }
      };
    return RatingServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void rateMod(rating.Rating.RateModRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.RateModResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRateModMethod(), responseObserver);
    }

    /**
     */
    default void getRates(rating.Rating.GetRatesRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.GetRatesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetRatesMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service RatingService.
   */
  public static abstract class RatingServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return RatingServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service RatingService.
   */
  public static final class RatingServiceStub
      extends io.grpc.stub.AbstractAsyncStub<RatingServiceStub> {
    private RatingServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RatingServiceStub(channel, callOptions);
    }

    /**
     */
    public void rateMod(rating.Rating.RateModRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.RateModResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRateModMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRates(rating.Rating.GetRatesRequest request,
        io.grpc.stub.StreamObserver<rating.Rating.GetRatesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetRatesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service RatingService.
   */
  public static final class RatingServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<RatingServiceBlockingStub> {
    private RatingServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RatingServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public rating.Rating.RateModResponse rateMod(rating.Rating.RateModRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRateModMethod(), getCallOptions(), request);
    }

    /**
     */
    public rating.Rating.GetRatesResponse getRates(rating.Rating.GetRatesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetRatesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service RatingService.
   */
  public static final class RatingServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<RatingServiceFutureStub> {
    private RatingServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RatingServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RatingServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<rating.Rating.RateModResponse> rateMod(
        rating.Rating.RateModRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRateModMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<rating.Rating.GetRatesResponse> getRates(
        rating.Rating.GetRatesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
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
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
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

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRateModMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              rating.Rating.RateModRequest,
              rating.Rating.RateModResponse>(
                service, METHODID_RATE_MOD)))
        .addMethod(
          getGetRatesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              rating.Rating.GetRatesRequest,
              rating.Rating.GetRatesResponse>(
                service, METHODID_GET_RATES)))
        .build();
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
    private final java.lang.String methodName;

    RatingServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
