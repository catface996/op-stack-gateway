package com.catface996.gateway.infrastructure.auth.client;

import com.catface996.gateway.application.auth.dto.AuthValidateRequest;
import com.catface996.gateway.application.auth.dto.AuthValidateResponse;
import com.catface996.gateway.domain.auth.model.AuthenticationResult;
import com.catface996.gateway.domain.auth.model.TokenInfo;
import com.catface996.gateway.domain.auth.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * HTTP client implementation for the auth service.
 * <p>
 * Calls the op-stack-auth service to validate JWT tokens and retrieve operator information.
 */
@Slf4j
@Component
public class AuthServiceClient implements AuthenticationService {

    private final WebClient webClient;
    private final String authServiceUrl;
    private final String validateEndpoint;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${gateway.services.op-stack-auth.url}") String authServiceUrl,
            @Value("${gateway.auth.validate-endpoint:/auth/validate}") String validateEndpoint) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
        this.authServiceUrl = authServiceUrl;
        this.validateEndpoint = validateEndpoint;
    }

    @Override
    public Mono<AuthenticationResult> authenticate(TokenInfo tokenInfo) {
        if (tokenInfo == null || !tokenInfo.isValid()) {
            return Mono.just(AuthenticationResult.failure("Invalid or missing token"));
        }

        AuthValidateRequest request = AuthValidateRequest.builder()
                .token(tokenInfo.getToken())
                .build();

        log.debug("Validating token with auth service at {}{}", authServiceUrl, validateEndpoint);

        return webClient.post()
                .uri(validateEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthValidateResponse.class)
                .map(this::toAuthenticationResult)
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError)
                .onErrorResume(Exception.class, this::handleGenericError);
    }

    private AuthenticationResult toAuthenticationResult(AuthValidateResponse response) {
        if (response.isSuccess() && response.getOperatorId() != null) {
            log.debug("Token validated successfully, operatorId: {}", response.getOperatorId());
            return AuthenticationResult.success(response.getOperatorId());
        } else {
            String message = response.getMessage() != null ? response.getMessage() : "Token validation failed";
            log.debug("Token validation failed: {}", message);
            return AuthenticationResult.failure(message);
        }
    }

    private Mono<AuthenticationResult> handleWebClientError(WebClientResponseException ex) {
        log.warn("Auth service returned error: {} {}", ex.getStatusCode(), ex.getMessage());
        if (ex.getStatusCode().is4xxClientError()) {
            return Mono.just(AuthenticationResult.failure("Invalid token"));
        }
        return Mono.just(AuthenticationResult.failure("Auth service unavailable"));
    }

    private Mono<AuthenticationResult> handleGenericError(Exception ex) {
        log.error("Error calling auth service: {}", ex.getMessage(), ex);
        return Mono.just(AuthenticationResult.failure("Authentication service error"));
    }
}
