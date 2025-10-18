package ratingservice;

import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratingservice.Settings.AppConfig;
import ratingservice.Settings.DataSourceFactory;
import ratingservice.Settings.LoggingConfigurator;
import ratingservice.handler.Handler;
import ratingservice.repository.Repository;
import ratingservice.service.Service;

public final class Server {

  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  private Server() {}

  public static void main(final String[] args) {
    AppConfig config;
    try {
      config = AppConfig.load();
    } catch (Exception e) {
      System.err.println("Failed to load configuration: " + e.getMessage());
      e.printStackTrace(System.err);
      System.exit(1);
      return;
    }

    LoggingConfigurator loggingConfigurator =
        new LoggingConfigurator(config.logLevel(), config.serviceName(), config.environment());
    loggingConfigurator.configure();

    ExecutorService workerPool = createWorkerPool(config);
    DataSource dataSource = null;
    try {
      dataSource = DataSourceFactory.create(config);
      Repository repository = new Repository(dataSource);
      Service service = new Service(repository, workerPool);
      Handler handler = new Handler(service);

      io.grpc.Server grpcServer = null;
      try {
        grpcServer = startServer(config.grpcPort(), handler);
        grpcServer.awaitTermination();
      } finally {
        shutdownServer(grpcServer);
      }
    } catch (Exception e) {
      LOGGER.error("Application terminated due to error", e);
      System.exit(1);
    } finally {
      closeDataSource(dataSource);
      shutdownExecutor(workerPool);
    }
  }

  private static io.grpc.Server startServer(final int port, final Handler handler)
      throws IOException {
    io.grpc.Server server =
        NettyServerBuilder.forPort(port)
            .addService(handler)
            .addService(ProtoReflectionService.newInstance())
            .build();
    server.start();
    LOGGER.info("gRPC server listening on port {}", port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  LOGGER.info("Shutdown requested - stopping gRPC server");
                  shutdownServer(server);
                },
                "grpc-shutdown-hook"));
    return server;
  }

  private static void shutdownServer(final io.grpc.Server server) {
    if (server == null) {
      return;
    }
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

  private static ExecutorService createWorkerPool(final AppConfig config) {
    int poolSize = Math.max(1, config.workerThreads());
    ThreadFactory delegate = Executors.defaultThreadFactory();
    AtomicInteger threadIndex = new AtomicInteger(1);
    return Executors.newFixedThreadPool(
        poolSize,
        runnable -> {
          Thread thread = delegate.newThread(runnable);
          thread.setName(config.serviceName() + "-worker-" + threadIndex.getAndIncrement());
          thread.setDaemon(true);
          return thread;
        });
  }

  private static void shutdownExecutor(final ExecutorService executor) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        executor.shutdownNow();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
          LOGGER.warn("Executor did not terminate cleanly");
        }
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
  }

  private static void closeDataSource(final DataSource dataSource) {
    if (dataSource instanceof AutoCloseable closeable) {
      try {
        closeable.close();
      } catch (Exception closeError) {
        LOGGER.warn("Failed to close data source gracefully", closeError);
      }
    }
  }
}
