package ratingservice;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GrpcServer implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);

  private final int port;
  private final Server server;

  public GrpcServer(final int port, final BindableService service) {
    this.port = port;
    Objects.requireNonNull(service, "service");
    this.server = NettyServerBuilder.forPort(port).addService(service).build();
  }

  public void start() throws IOException {
    server.start();
    LOGGER.info("gRPC server listening on port {}", port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  LOGGER.info("Shutdown requested - stopping gRPC server");
                  close();
                },
                "grpc-shutdown-hook"));
  }

  public void blockUntilShutdown() throws InterruptedException {
    server.awaitTermination();
  }

  @Override
  public void close() {
    server.shutdown();
    try {
      if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
        server.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      server.shutdownNow();
    }
  }
}

