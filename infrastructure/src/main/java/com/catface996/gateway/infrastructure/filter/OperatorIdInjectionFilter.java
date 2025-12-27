package com.catface996.gateway.infrastructure.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static com.catface996.gateway.common.constants.RouteConstants.METADATA_KEY_PUBLIC;

/**
 * Global filter that injects the operatorId into the request body for authenticated requests.
 * <p>
 * This filter:
 * <ul>
 *     <li>Only processes POST requests with JSON body</li>
 *     <li>Skips public routes (no authentication = no operatorId)</li>
 *     <li>Reads the operator ID from exchange attributes (set by AuthenticationGatewayFilter)</li>
 *     <li>Adds or updates the "operatorId" field in the JSON request body</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperatorIdInjectionFilter implements GlobalFilter, Ordered {

    /**
     * Filter order - runs after AuthenticationGatewayFilter.
     */
    public static final int ORDER = AuthenticationGatewayFilter.ORDER + 100;

    /**
     * The JSON field name for the operator ID.
     */
    public static final String OPERATOR_ID_FIELD = "operatorId";

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip if public route
        if (isPublicRoute(exchange)) {
            return chain.filter(exchange);
        }

        // Skip if not POST request
        if (!HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        // Skip if not JSON content type
        MediaType contentType = exchange.getRequest().getHeaders().getContentType();
        if (contentType == null || !contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return chain.filter(exchange);
        }

        // Get operator ID from exchange attributes (set by AuthenticationGatewayFilter)
        Long operatorId = exchange.getAttribute(AuthenticationGatewayFilter.OPERATOR_ID_ATTR);
        if (operatorId == null) {
            log.debug("No operatorId found in exchange attributes, skipping injection");
            return chain.filter(exchange);
        }

        // Read and modify request body
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String originalBody = new String(bytes, StandardCharsets.UTF_8);
                    String modifiedBody = injectOperatorId(originalBody, operatorId);

                    return continueWithModifiedBody(exchange, chain, modifiedBody);
                });
    }

    private boolean isPublicRoute(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return false;
        }
        Object publicMetadata = route.getMetadata().get(METADATA_KEY_PUBLIC);
        return Boolean.TRUE.equals(publicMetadata);
    }

    private String injectOperatorId(String originalBody, Long operatorId) {
        try {
            JsonNode rootNode;
            if (originalBody == null || originalBody.isEmpty()) {
                rootNode = objectMapper.createObjectNode();
            } else {
                rootNode = objectMapper.readTree(originalBody);
            }

            if (rootNode instanceof ObjectNode objectNode) {
                objectNode.put(OPERATOR_ID_FIELD, operatorId);
                String modifiedBody = objectMapper.writeValueAsString(objectNode);
                log.debug("Injected operatorId {} into request body", operatorId);
                return modifiedBody;
            } else {
                log.warn("Request body is not a JSON object, cannot inject operatorId");
                return originalBody;
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing/modifying request body JSON", e);
            return originalBody;
        }
    }

    private Mono<Void> continueWithModifiedBody(ServerWebExchange exchange, GatewayFilterChain chain,
                                                  String modifiedBody) {
        byte[] modifiedBytes = modifiedBody.getBytes(StandardCharsets.UTF_8);

        ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(modifiedBytes);
                return Flux.just(buffer);
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.setContentLength(modifiedBytes.length);
                return headers;
            }
        };

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        return chain.filter(modifiedExchange);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
