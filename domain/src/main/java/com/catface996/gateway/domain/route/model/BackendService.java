package com.catface996.gateway.domain.route.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Value object representing a backend service that the gateway proxies to.
 * <p>
 * Each backend service has a unique identifier, URL, and associated path prefix.
 */
@Getter
@Builder
@AllArgsConstructor
public class BackendService {

    /**
     * Unique identifier for the service (e.g., "op-stack-service")
     */
    private final String serviceId;

    /**
     * Base URL of the service (e.g., "http://localhost:8081")
     */
    private final String url;

    /**
     * Path prefix that routes to this service (e.g., "/api/service")
     */
    private final String pathPrefix;

    /**
     * Human-readable name of the service
     */
    private final String displayName;

    /**
     * Checks if this service matches the given path.
     *
     * @param path the request path to check
     * @return true if the path starts with this service's path prefix
     */
    public boolean matchesPath(String path) {
        return path != null && path.startsWith(pathPrefix);
    }
}
