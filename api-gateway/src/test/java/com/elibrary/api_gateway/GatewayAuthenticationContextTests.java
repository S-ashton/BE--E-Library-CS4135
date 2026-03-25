package com.elibrary.api_gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "logging.level.org.springframework.web.server.adapter.HttpWebHandlerAdapter=DEBUG")
@AutoConfigureWebTestClient
@ExtendWith({SpringExtension.class, OutputCaptureExtension.class})
class GatewayAuthenticationContextTests {

    private static final DisposableServer DOWNSTREAM_SERVER = HttpServer.create()
            .port(0)
            .route(routes -> routes
                    .get("/api/auth/ping", (request, response) -> response.sendString(Mono.just("public")))
                    .get("/api/books/echo-auth", (request, response) -> {
                        String authorizationHeader = request.requestHeaders().get(HttpHeaders.AUTHORIZATION);
                        return response.sendString(Mono.just(authorizationHeader == null ? "missing" : authorizationHeader));
                    })
                    .get("/api/users/echo-identity", (request, response) -> {
                        String authenticatedUser = request.requestHeaders().get("X-Authenticated-User");
                        return response.sendString(Mono.just(authenticatedUser == null ? "missing" : authenticatedUser));
                    }))
            .bindNow();

    private static final String DOWNSTREAM_BASE_URL = "http://localhost:" + DOWNSTREAM_SERVER.port();

    @Autowired
    private WebTestClient webTestClient;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    @DynamicPropertySource
    static void overrideRouteUris(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "user-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].uri", () -> DOWNSTREAM_BASE_URL);
        registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/api/auth/**,/api/users/**");
        registry.add("spring.cloud.gateway.server.webflux.routes[1].id", () -> "book-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[1].uri", () -> DOWNSTREAM_BASE_URL);
        registry.add("spring.cloud.gateway.server.webflux.routes[1].predicates[0]", () -> "Path=/api/books/**");
    }

    @AfterAll
    static void stopServer() {
        DOWNSTREAM_SERVER.disposeNow();
    }

    @Test
    void publicEndpointsMayRouteWithoutAuthorizationHeader() {
        webTestClient.get()
                .uri("/api/auth/ping")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("public");
    }

    @Test
    void protectedEndpointsRequireAuthorizationHeader() {
        webTestClient.get()
                .uri("/api/books/echo-auth")
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Authentication token is required");
    }

    @Test
    void validJwtIsForwardedAndAuthenticatedIdentityHeadersAreAdded() {
        String authorizationHeader = "Bearer " + buildValidToken("reader@elibrary.ie", "USER", 42L);

        webTestClient.get()
                .uri("/api/books/echo-auth")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(authorizationHeader);

        webTestClient.get()
                .uri("/api/users/echo-identity")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("reader@elibrary.ie");
    }

    @Test
    void expiredJwtIsRejected() {
        webTestClient.get()
                .uri("/api/books/echo-auth")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildExpiredToken("reader@elibrary.ie", "USER", 42L))
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Token has expired");
    }

    @Test
    void alteredJwtIsRejected() {
        String validToken = buildValidToken("reader@elibrary.ie", "USER", 42L);
        String alteredToken = validToken.substring(0, validToken.length() - 1)
                + (validToken.endsWith("a") ? "b" : "a");

        webTestClient.get()
                .uri("/api/books/echo-auth")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + alteredToken)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Invalid authentication token");
    }

    @Test
    void authorizationHeaderIsNotLoggedInPlaintext(CapturedOutput output) {
        String authorizationHeader = "Bearer " + buildValidToken("reader@elibrary.ie", "USER", 42L);

        webTestClient.get()
                .uri("/api/books/echo-auth")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(output).contains("Completed 200 OK");
        assertThat(output).doesNotContain(authorizationHeader);
    }

    private String buildValidToken(String email, String role, Long userId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(3600);
        return buildToken(email, role, userId, issuedAt, expiresAt);
    }

    private String buildExpiredToken(String email, String role, Long userId) {
        Instant issuedAt = Instant.now().minusSeconds(7200);
        Instant expiresAt = issuedAt.plusSeconds(60);
        return buildToken(email, role, userId, issuedAt, expiresAt);
    }

    private String buildToken(String email, String role, Long userId, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(email)
                .issuer(jwtIssuer)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
