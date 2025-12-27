package com.catface996.gateway.infrastructure.config;

import com.catface996.gateway.common.dto.ErrorResponse;
import com.catface996.gateway.common.enums.ErrorCode;
import com.catface996.gateway.common.exception.AuthenticationException;
import com.catface996.gateway.common.exception.GatewayException;
import com.catface996.gateway.common.exception.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Global exception handler for the gateway.
 * Converts exceptions to standardized error responses.
 */
@Slf4j
@Component
@Order(-1) // High priority to handle exceptions before default handler
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getPath().value();

        ErrorResponse errorResponse;
        HttpStatus status;

        if (ex instanceof AuthenticationException authEx) {
            status = HttpStatus.UNAUTHORIZED;
            errorResponse = ErrorResponse.of(
                    authEx.getCode(),
                    authEx.getMessage(),
                    path
            );
            log.warn("Authentication failed for path {}: {}", path, authEx.getMessage());

        } else if (ex instanceof ServiceUnavailableException svcEx) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorResponse = ErrorResponse.of(
                    svcEx.getCode(),
                    svcEx.getMessage(),
                    path
            );
            log.error("Service unavailable for path {}: {}", path, svcEx.getMessage());

        } else if (ex instanceof GatewayException gatewayEx) {
            status = HttpStatus.valueOf(gatewayEx.getHttpStatus());
            errorResponse = ErrorResponse.of(
                    gatewayEx.getCode(),
                    gatewayEx.getMessage(),
                    path
            );
            log.warn("Gateway error for path {}: {}", path, gatewayEx.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            errorResponse = ErrorResponse.of(
                    status.name(),
                    rse.getReason() != null ? rse.getReason() : status.getReasonPhrase(),
                    path
            );
            log.warn("Response status exception for path {}: {}", path, rse.getMessage());

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = ErrorResponse.of(
                    ErrorCode.INTERNAL_ERROR.getCode(),
                    "An unexpected error occurred",
                    path
            );
            log.error("Unexpected error for path {}", path, ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            byte[] fallback = "{\"code\":\"INTERNAL_ERROR\",\"message\":\"Error processing response\"}"
                    .getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }
}
