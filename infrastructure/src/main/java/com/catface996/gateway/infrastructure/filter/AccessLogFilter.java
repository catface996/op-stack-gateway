package com.catface996.gateway.infrastructure.filter;

import com.catface996.gateway.common.constants.RouteConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Global filter that logs access information for all requests.
 * <p>
 * Logs the following information:
 * <ul>
 *     <li>Request ID (generated or from header)</li>
 *     <li>HTTP method and path</li>
 *     <li>Target route/service</li>
 *     <li>Response status code</li>
 *     <li>Request duration in milliseconds</li>
 * </ul>
 */
@Slf4j
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    /**
     * Filter order - should run first to capture timing accurately.
     */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();
        ServerHttpRequest request = exchange.getRequest();

        // Get or generate request ID
        String requestId = Optional.ofNullable(request.getHeaders().getFirst(RouteConstants.HEADER_REQUEST_ID))
                .orElse(UUID.randomUUID().toString());

        // Add request ID to exchange attributes for downstream use
        exchange.getAttributes().put(RouteConstants.HEADER_REQUEST_ID, requestId);

        // Mutate request to include request ID header if not present
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(RouteConstants.HEADER_REQUEST_ID, requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String clientIp = getClientIp(request);

        log.info("[{}] --> {} {} from {}", requestId, method, path, clientIp);

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() -> logResponse(mutatedExchange, requestId, method, path, startTime)));
    }

    private void logResponse(ServerWebExchange exchange, String requestId, String method, String path, Instant startTime) {
        ServerHttpResponse response = exchange.getResponse();
        HttpStatusCode statusCode = response.getStatusCode();

        long durationMs = Duration.between(startTime, Instant.now()).toMillis();

        String routeId = getRouteId(exchange);

        int status = statusCode != null ? statusCode.value() : 0;

        if (status >= 500) {
            log.error("[{}] <-- {} {} {} {}ms route={}", requestId, method, path, status, durationMs, routeId);
        } else if (status >= 400) {
            log.warn("[{}] <-- {} {} {} {}ms route={}", requestId, method, path, status, durationMs, routeId);
        } else {
            log.info("[{}] <-- {} {} {} {}ms route={}", requestId, method, path, status, durationMs, routeId);
        }
    }

    private String getRouteId(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route != null ? route.getId() : "unknown";
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check X-Forwarded-For header first (for proxied requests)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP if multiple are present
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
