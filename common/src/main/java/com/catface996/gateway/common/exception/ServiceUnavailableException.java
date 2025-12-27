package com.catface996.gateway.common.exception;

import com.catface996.gateway.common.enums.ErrorCode;

/**
 * Exception for backend service unavailability.
 * Thrown when a backend service is unreachable or times out.
 */
public class ServiceUnavailableException extends GatewayException {

    public ServiceUnavailableException() {
        super(ErrorCode.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String message) {
        super(ErrorCode.SERVICE_UNAVAILABLE, message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(ErrorCode.SERVICE_UNAVAILABLE, message, cause);
    }
}
