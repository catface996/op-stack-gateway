package com.catface996.gateway.bootstrap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.HashSet;
import java.util.Set;

/**
 * Swagger/OpenAPI configuration for aggregating API documentation from multiple backend services.
 * <p>
 * This configuration provides a unified Swagger UI at the gateway that allows users to
 * browse and test APIs from all backend services.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Gateway's own OpenAPI definition.
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Op-Stack Gateway API")
                        .description("API Gateway for Op-Stack microservices")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("catface996")
                                .email("catface996@example.com")));
    }

    /**
     * Configure Swagger UI to show all backend services' API docs.
     */
    @Bean
    @Lazy(false)
    public Set<SwaggerUrl> swaggerUrls(SwaggerUiConfigProperties swaggerUiConfigProperties) {
        Set<SwaggerUrl> urls = new HashSet<>();

        // Use gateway-proxied paths to avoid CORS issues
        // Gateway routes /docs/{service}/** to each backend service
        urls.add(new SwaggerUrl("op-stack-service", "/docs/service/v3/api-docs", "Op-Stack Service API"));
        urls.add(new SwaggerUrl("op-stack-executor", "/docs/executor/v3/api-docs", "Op-Stack Executor API"));
        urls.add(new SwaggerUrl("op-stack-tools", "/docs/tools/v3/api-docs", "Op-Stack Tools API"));
        urls.add(new SwaggerUrl("op-stack-auth", "/docs/auth/v3/api-docs", "Op-Stack Auth API"));

        swaggerUiConfigProperties.setUrls(urls);
        return urls;
    }

    /**
     * Gateway's own API group (for gateway management endpoints if any).
     */
    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("gateway")
                .displayName("Gateway Management API")
                .pathsToMatch("/actuator/**")
                .build();
    }
}
