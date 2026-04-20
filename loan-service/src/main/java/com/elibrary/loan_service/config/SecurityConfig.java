package com.elibrary.loan_service.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        GatewayIdentityAuthenticationFilter gatewayIdentityAuthenticationFilter,
        JsonAuthenticationEntryPoint authenticationEntryPoint,
        JsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                // Internal endpoint called directly by book-service (no user JWT present)
                .requestMatchers(HttpMethod.GET, "/api/loans/has-active-by-book").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/loans").hasAnyRole("USER", "STAFF", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/loans/*/return").hasAnyRole("USER", "STAFF", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/loans/history").hasAnyRole("USER", "STAFF", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/loans/active").hasAnyRole("STAFF", "ADMIN")
                .anyRequest().denyAll()
            )
            .addFilterBefore(gatewayIdentityAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<GatewayIdentityAuthenticationFilter> gatewayIdentityAuthenticationFilterRegistration(
        GatewayIdentityAuthenticationFilter gatewayIdentityAuthenticationFilter
    ) {
        FilterRegistrationBean<GatewayIdentityAuthenticationFilter> registration =
            new FilterRegistrationBean<>(gatewayIdentityAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }
}
