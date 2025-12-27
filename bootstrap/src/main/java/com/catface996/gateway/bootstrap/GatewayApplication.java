package com.catface996.gateway.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway Application entry point.
 * <p>
 * This gateway serves as an HTTP reverse proxy for:
 * - op-stack-service
 * - op-stack-executor
 * - op-stack-tools
 * - op-stack-auth
 */
@SpringBootApplication(scanBasePackages = "com.catface996.gateway")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
