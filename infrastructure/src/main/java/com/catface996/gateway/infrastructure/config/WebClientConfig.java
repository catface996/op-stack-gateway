package com.catface996.gateway.infrastructure.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuration for WebClient used by infrastructure components.
 * <p>
 * Provides a pre-configured WebClient for making HTTP calls to backend services
 * such as the auth service for token validation.
 */
@Configuration
public class WebClientConfig {

    @Value("${gateway.webclient.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${gateway.webclient.response-timeout-ms:10000}")
    private int responseTimeoutMs;

    /**
     * Creates a WebClient with connection and response timeout settings.
     *
     * @return configured WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    /**
     * Creates a default WebClient instance.
     *
     * @param webClientBuilder the pre-configured builder
     * @return WebClient instance
     */
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
