package com.catface996.gateway.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of gateway error codes.
 * Maps to HTTP status codes for standardized error responses.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * Invalid, expired, or missing JWT token
     */
    UNAUTHORIZED(401, "UNAUTHORIZED", "Authentication required"),

    /**
     * Token valid but insufficient permissions
     */
    FORBIDDEN(403, "FORBIDDEN", "Access denied"),

    /**
     * No route matches the request path
     */
    NOT_FOUND(404, "NOT_FOUND", "Resource not found"),

    /**
     * Unexpected gateway error
     */
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "Internal server error"),

    /**
     * Invalid response from backend service
     */
    BAD_GATEWAY(502, "BAD_GATEWAY", "Bad gateway"),

    /**
     * Backend service unreachable
     */
    SERVICE_UNAVAILABLE(503, "SERVICE_UNAVAILABLE", "Service temporarily unavailable");

    private final int httpStatus;
    private final String code;
    private final String defaultMessage;
}
