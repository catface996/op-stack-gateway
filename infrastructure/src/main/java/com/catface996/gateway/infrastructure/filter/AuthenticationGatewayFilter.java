package com.catface996.gateway.infrastructure.filter;

import com.catface996.gateway.common.constants.RouteConstants;
import com.catface996.gateway.common.dto.ErrorResponse;
import com.catface996.gateway.common.enums.ErrorCode;
import com.catface996.gateway.domain.auth.model.AuthenticationResult;
import com.catface996.gateway.domain.auth.model.TokenInfo;
import com.catface996.gateway.domain.auth.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter that authenticates requests to protected routes.
 * <p>
 * This filter:
 * <ul>
 *     <li>Skips authentication for routes marked as public (metadata.public=true)</li>
 *     <li>Extracts JWT token from Authorization header</li>
 *     <li>Validates token via the auth service</li>
 *     <li>Stores operatorId in exchange attributes for downstream filters</li>
 *     <li>Returns 401 Unauthorized for invalid or missing tokens</li>
 * </ul>
 */
@Slf4j
@Component
public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {

    /**
     * Filter order - runs after AccessLogFilter but before OperatorIdInjectionFilter.
     */
    public static final int ORDER = AccessLogFilter.ORDER + 100;

    /**
     * Exchange attribute key for storing the authenticated operator ID.
     */
    public static final String OPERATOR_ID_ATTR = "operatorId";

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    private final boolean authEnabled;

    public AuthenticationGatewayFilter(
            AuthenticationService authenticationService,
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${gateway.auth.enabled:true}") boolean authEnabled) {
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
        this.authEnabled = authEnabled;
        log.info("Authentication filter initialized, enabled: {}", authEnabled);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Check if authentication is disabled globally
        if (!authEnabled) {
            log.debug("Authentication disabled, skipping");
            return chain.filter(exchange);
        }

        // Check if route is public
        if (isPublicRoute(exchange)) {
            log.debug("Public route, skipping authentication");
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(RouteConstants.HEADER_AUTHORIZATION);
        TokenInfo tokenInfo = TokenInfo.fromBearerToken(authHeader);

        if (tokenInfo == null) {
            log.debug("Missing or invalid Authorization header");
            return writeUnauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        // Validate token with auth service
        return authenticationService.authenticate(tokenInfo)
                .flatMap(result -> handleAuthenticationResult(exchange, chain, result));
    }

    private boolean isPublicRoute(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return false;
        }

        Object publicMetadata = route.getMetadata().get(RouteConstants.METADATA_KEY_PUBLIC);
        return Boolean.TRUE.equals(publicMetadata);
    }

    private Mono<Void> handleAuthenticationResult(ServerWebExchange exchange, GatewayFilterChain chain,
                                                   AuthenticationResult result) {
        if (result.isAuthenticated()) {
            // Store operator ID in exchange attributes for OperatorIdInjectionFilter
            exchange.getAttributes().put(OPERATOR_ID_ATTR, result.getOperatorId());
            log.debug("Authentication successful, operatorId: {}", result.getOperatorId());
            return chain.filter(exchange);
        } else {
            log.debug("Authentication failed: {}", result.getErrorMessage());
            return writeUnauthorizedResponse(exchange, result.getErrorMessage());
        }
    }

    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .message(message)
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
