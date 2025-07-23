package com.esclient.ratingservice.config;

import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class GrpcServerConfig {

    @Bean
    public ServerInterceptor metricInterceptor(MeterRegistry registry) {
        return new MetricCollectingServerInterceptor(registry);
    }

    @Bean
    public GrpcServerConfigurer serverConfigurer(ServerInterceptor metricInterceptor) {
        return serverBuilder -> serverBuilder.intercept(metricInterceptor);
    }
}
