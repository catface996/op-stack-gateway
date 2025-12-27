package com.catface996.gateway.common.constants;

import java.util.List;

/**
 * Constants for public (unauthenticated) routes.
 * <p>
 * These routes do not require JWT authentication.
 */
public final class PublicRouteConstants {

    private PublicRouteConstants() {
        // Utility class, prevent instantiation
    }

    // ========== Public Paths ==========

    /**
     * User registration endpoint path
     */
    public static final String REGISTER_PATH = "/api/auth/register";

    /**
     * User login endpoint path
     */
    public static final String LOGIN_PATH = "/api/auth/login";

    /**
     * List of all public paths that do not require authentication.
     */
    public static final List<String> PUBLIC_PATHS = List.of(
            REGISTER_PATH,
            LOGIN_PATH
    );

    /**
     * Checks if the given path is a public route.
     *
     * @param path the request path to check
     * @return true if the path is a public route
     */
    public static boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
