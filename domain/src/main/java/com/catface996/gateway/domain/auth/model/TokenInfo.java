package com.catface996.gateway.domain.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Value object representing JWT token information.
 * <p>
 * Contains the raw token string extracted from the Authorization header.
 */
@Getter
@Builder
@AllArgsConstructor
public class TokenInfo {

    /**
     * The raw JWT token string (without "Bearer " prefix)
     */
    private final String token;

    /**
     * Creates a TokenInfo from a Bearer token header value.
     *
     * @param authorizationHeader the full Authorization header value (e.g., "Bearer xxx")
     * @return TokenInfo with the extracted token, or null if invalid
     */
    public static TokenInfo fromBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            return null;
        }
        return new TokenInfo(token);
    }

    /**
     * Checks if the token is present and non-empty.
     *
     * @return true if the token is valid
     */
    public boolean isValid() {
        return token != null && !token.isEmpty();
    }
}
