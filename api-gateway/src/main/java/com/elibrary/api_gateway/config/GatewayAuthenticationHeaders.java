package com.elibrary.api_gateway.config;

public final class GatewayAuthenticationHeaders {

    public static final String AUTHENTICATED_USER = "X-Authenticated-User";
    public static final String AUTHENTICATED_ROLE = "X-Authenticated-Role";
    public static final String AUTHENTICATED_USER_ID = "X-Authenticated-User-Id";

    private GatewayAuthenticationHeaders() {
    }
}
