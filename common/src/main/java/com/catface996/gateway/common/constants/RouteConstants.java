package com.catface996.gateway.common.constants;

/**
 * Constants for gateway routing configuration.
 * <p>
 * Defines path prefixes for all backend services.
 */
public final class RouteConstants {

    private RouteConstants() {
        // Utility class, prevent instantiation
    }

    // ========== Service Path Prefixes ==========

    /**
     * Path prefix for op-stack-service routes
     */
    public static final String SERVICE_PATH_PREFIX = "/api/service";

    /**
     * Path prefix for op-stack-executor routes
     */
    public static final String EXECUTOR_PATH_PREFIX = "/api/executor";

    /**
     * Path prefix for op-stack-tools routes
     */
    public static final String TOOLS_PATH_PREFIX = "/api/tools";

    /**
     * Path prefix for op-stack-auth routes
     */
    public static final String AUTH_PATH_PREFIX = "/api/auth";

    // ========== Service Identifiers ==========

    /**
     * Service ID for op-stack-service
     */
    public static final String SERVICE_ID_SERVICE = "op-stack-service";

    /**
     * Service ID for op-stack-executor
     */
    public static final String SERVICE_ID_EXECUTOR = "op-stack-executor";

    /**
     * Service ID for op-stack-tools
     */
    public static final String SERVICE_ID_TOOLS = "op-stack-tools";

    /**
     * Service ID for op-stack-auth
     */
    public static final String SERVICE_ID_AUTH = "op-stack-auth";

    // ========== Route Metadata Keys ==========

    /**
     * Metadata key indicating if a route is public (no auth required)
     */
    public static final String METADATA_KEY_PUBLIC = "public";

    /**
     * Metadata key for the target service identifier
     */
    public static final String METADATA_KEY_SERVICE = "service";

    // ========== HTTP Headers ==========

    /**
     * Authorization header name
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Bearer token prefix
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Request ID header for tracing
     */
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
}
