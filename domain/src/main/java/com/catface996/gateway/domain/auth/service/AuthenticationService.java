package com.catface996.gateway.domain.auth.service;

import com.catface996.gateway.domain.auth.model.AuthenticationResult;
import com.catface996.gateway.domain.auth.model.TokenInfo;
import reactor.core.publisher.Mono;

/**
 * Domain service interface for token authentication.
 * <p>
 * Implementations should validate JWT tokens against the auth service
 * and return the authenticated operator information.
 */
public interface AuthenticationService {

    /**
     * Validates a JWT token and returns the authentication result.
     *
     * @param tokenInfo the token information to validate
     * @return a Mono containing the authentication result
     */
    Mono<AuthenticationResult> authenticate(TokenInfo tokenInfo);
}
