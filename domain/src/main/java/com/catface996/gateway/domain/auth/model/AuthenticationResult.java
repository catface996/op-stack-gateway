package com.catface996.gateway.domain.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Value object representing the result of token authentication.
 * <p>
 * Contains the authenticated operator ID on success, or error information on failure.
 */
@Getter
@Builder
@AllArgsConstructor
public class AuthenticationResult {

    /**
     * Whether authentication was successful
     */
    private final boolean authenticated;

    /**
     * The operator ID from the validated token (null if authentication failed)
     */
    private final Long operatorId;

    /**
     * Error message if authentication failed (null if successful)
     */
    private final String errorMessage;

    /**
     * Creates a successful authentication result.
     *
     * @param operatorId the authenticated operator's ID
     * @return successful AuthenticationResult
     */
    public static AuthenticationResult success(Long operatorId) {
        return AuthenticationResult.builder()
                .authenticated(true)
                .operatorId(operatorId)
                .build();
    }

    /**
     * Creates a failed authentication result.
     *
     * @param errorMessage the reason for failure
     * @return failed AuthenticationResult
     */
    public static AuthenticationResult failure(String errorMessage) {
        return AuthenticationResult.builder()
                .authenticated(false)
                .errorMessage(errorMessage)
                .build();
    }
}
