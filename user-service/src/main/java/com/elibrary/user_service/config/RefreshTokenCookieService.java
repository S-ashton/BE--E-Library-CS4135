package com.elibrary.user_service.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieService {

    private final JwtProperties jwtProperties;

    public RefreshTokenCookieService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(refreshToken, jwtProperties.getRefreshExpirationSeconds()).toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", 0).toString());
    }

    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (jwtProperties.getRefreshCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public String getCookieName() {
        return jwtProperties.getRefreshCookieName();
    }

    private ResponseCookie buildCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(jwtProperties.getRefreshCookieName(), value)
            .httpOnly(true)
            .secure(jwtProperties.isRefreshCookieSecure())
            .sameSite(jwtProperties.getRefreshCookieSameSite())
            .path(jwtProperties.getRefreshCookiePath())
            .maxAge(Duration.ofSeconds(maxAgeSeconds))
            .build();
    }
}
