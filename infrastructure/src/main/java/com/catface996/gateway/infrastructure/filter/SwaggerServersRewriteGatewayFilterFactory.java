package com.catface996.gateway.infrastructure.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway filter that rewrites the 'servers' field in OpenAPI documentation
 * to point to the gateway instead of the backend service directly.
 * <p>
 * This ensures that when using Swagger UI through the gateway, API requests
 * are routed through the gateway rather than directly to backend services.
 */
@Slf4j
@Component
public class SwaggerServersRewriteGatewayFilterFactory extends AbstractGatewayFilterFactory<SwaggerServersRewriteGatewayFilterFactory.Config> {

    private final ObjectMapper objectMapper;

    public SwaggerServersRewriteGatewayFilterFactory(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new SwaggerServersRewriteFilter(config);
    }

    private class SwaggerServersRewriteFilter implements GatewayFilter, Ordered {

        private final Config config;

        public SwaggerServersRewriteFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String path = exchange.getRequest().getURI().getPath();

            // Only process API docs requests
            if (!path.contains("/v3/api-docs")) {
                return chain.filter(exchange);
            }

            // Get the gateway base URL (no API prefix - backend paths already include full path)
            String gatewayBaseUrl = getGatewayBaseUrl(exchange);

            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                        return super.writeWith(
                                DataBufferUtils.join(fluxBody)
                                        .map(dataBuffer -> {
                                            byte[] content = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(content);
                                            DataBufferUtils.release(dataBuffer);

                                            String responseBody = new String(content, StandardCharsets.UTF_8);
                                            String modifiedBody = rewriteServers(responseBody, gatewayBaseUrl);

                                            byte[] modifiedContent = modifiedBody.getBytes(StandardCharsets.UTF_8);

                                            // Update Content-Length header
                                            originalResponse.getHeaders().setContentLength(modifiedContent.length);

                                            return bufferFactory.wrap(modifiedContent);
                                        })
                        );
                    }
                    return super.writeWith(body);
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }

        private String getGatewayBaseUrl(ServerWebExchange exchange) {
            String scheme = exchange.getRequest().getURI().getScheme();
            String host = exchange.getRequest().getURI().getHost();
            int port = exchange.getRequest().getURI().getPort();

            if (port == -1 || port == 80 || port == 443) {
                return scheme + "://" + host;
            }
            return scheme + "://" + host + ":" + port;
        }

        private String rewriteServers(String responseBody, String gatewayBaseUrl) {
            try {
                JsonNode root = objectMapper.readTree(responseBody);

                if (root.isObject()) {
                    ObjectNode rootObject = (ObjectNode) root;

                    // Create new servers array with gateway URL
                    // No API prefix needed - backend paths already include full path like /api/service/...
                    ArrayNode newServers = objectMapper.createArrayNode();
                    ObjectNode gatewayServer = objectMapper.createObjectNode();
                    gatewayServer.put("url", gatewayBaseUrl);
                    gatewayServer.put("description", "API Gateway");
                    newServers.add(gatewayServer);

                    // Replace servers field
                    rootObject.set("servers", newServers);

                    return objectMapper.writeValueAsString(rootObject);
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to rewrite servers in API docs: {}", e.getMessage());
            }

            return responseBody;
        }

        @Override
        public int getOrder() {
            // Run before NettyWriteResponseFilter
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
        }
    }

    public static class Config {
        // Configuration properties if needed in the future
    }
}
