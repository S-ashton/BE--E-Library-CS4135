package com.elibrary.recommendation_service.config;

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
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/circuitbreakers").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/recommendations/internal/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/recommendations").hasAnyRole("USER", "STAFF", "ADMIN")
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
