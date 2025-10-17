package ratingservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import ratingservice.config.AppConfig;
import ratingservice.config.DataSourceFactory;
import ratingservice.config.DatabaseInitializer;
import ratingservice.config.LoggingConfigurator;
import ratingservice.handler.Handler;
import ratingservice.repository.Repository;
import ratingservice.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceApplication.class);

  private ServiceApplication() {
    // Utility class
  }

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
      DatabaseInitializer initializer = new DatabaseInitializer();
      initializer.initialize(dataSource);

      Repository repository = new Repository(dataSource);
      Service service = new Service(repository, workerPool);
      Handler handler = new Handler(service);

      try (GrpcServer server = new GrpcServer(config.grpcPort(), handler)) {
        server.start();
        LOGGER.info("Rating Service started successfully");
        server.blockUntilShutdown();
      }
    } catch (Exception e) {
      LOGGER.error("Application terminated due to error", e);
      System.exit(1);
    } finally {
      closeDataSource(dataSource);
      shutdownExecutor(workerPool);
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

