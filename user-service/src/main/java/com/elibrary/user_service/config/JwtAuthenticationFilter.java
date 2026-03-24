package com.elibrary.user_service.config;

import com.elibrary.user_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        UserDetailsService userDetailsService,
        AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth")
            || path.startsWith("/api/auth/")
            || path.equals("/actuator/health")
            || path.equals("/actuator/info")
            || path.equals("/swagger-ui.html")
            || path.startsWith("/swagger-ui/")
            || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authorizationHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            reject(request, response, "Invalid authentication token", new BadCredentialsException("Invalid authentication token"));
            return;
        }

        String token = authorizationHeader.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            reject(request, response, "Invalid authentication token", new BadCredentialsException("Invalid authentication token"));
            return;
        }

        try {
            Claims claims = jwtService.parseToken(token);
            String email = claims.getSubject();
            if (!StringUtils.hasText(email)) {
                throw new BadCredentialsException("Invalid authentication token");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (!userDetails.isEnabled()) {
                    throw new DisabledException("Invalid authentication token");
                }

                var authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    token,
                    userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            reject(request, response, "Token has expired", new CredentialsExpiredException("Token has expired", ex));
        } catch (JwtException | AuthenticationException | IllegalArgumentException ex) {
            reject(request, response, "Invalid authentication token", ex instanceof AuthenticationException authEx
                ? authEx
                : new BadCredentialsException("Invalid authentication token", ex));
        }
    }

    private void reject(
        HttpServletRequest request,
        HttpServletResponse response,
        String message,
        AuthenticationException exception
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        request.setAttribute("auth_error_message", message);
        authenticationEntryPoint.commence(request, response, exception);
    }
}
