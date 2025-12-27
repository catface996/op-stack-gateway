package com.catface996.gateway.common.exception;

import com.catface996.gateway.common.enums.ErrorCode;

/**
 * Exception for authentication failures.
 * Thrown when JWT token is invalid, expired, or missing.
 */
public class AuthenticationException extends GatewayException {

    public AuthenticationException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public AuthenticationException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHORIZED, message, cause);
    }
}
