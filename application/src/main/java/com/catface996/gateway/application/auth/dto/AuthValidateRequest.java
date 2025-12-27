package com.catface996.gateway.application.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for validating JWT tokens with the auth service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthValidateRequest {

    /**
     * The JWT token to validate
     */
    private String token;
}
