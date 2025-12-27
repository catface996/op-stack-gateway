package com.catface996.gateway.bootstrap.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for gateway filters.
 * <p>
 * This configuration ensures all filter and auth client components from the infrastructure module
 * are scanned and registered as Spring beans.
 * <p>
 * Registered filters:
 * <ul>
 *     <li>{@code AccessLogFilter} - Logs all incoming requests and responses</li>
 *     <li>{@code AuthenticationGatewayFilter} - Validates JWT tokens for protected routes</li>
 *     <li>{@code OperatorIdInjectionFilter} - Injects operatorId into request body</li>
 * </ul>
 */
@Configuration
@ComponentScan(basePackages = {
        "com.catface996.gateway.infrastructure.filter",
        "com.catface996.gateway.infrastructure.auth",
        "com.catface996.gateway.infrastructure.config"
})
public class FilterConfig {
    // Filter beans are auto-discovered via component scanning
    // Additional filter-related beans can be defined here if needed
}
