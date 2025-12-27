package com.catface996.gateway.application.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from the auth service token validation endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthValidateResponse {

    /**
     * Whether the token is valid
     */
    private Boolean success;

    /**
     * The operator ID associated with the token (if valid)
     */
    private Long operatorId;

    /**
     * Error message if validation failed
     */
    private String message;

    /**
     * Checks if the authentication was successful.
     *
     * @return true if the token was validated successfully
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
}
