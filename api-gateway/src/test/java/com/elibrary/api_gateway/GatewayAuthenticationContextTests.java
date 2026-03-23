package com.elibrary.api_gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "logging.level.org.springframework.web.server.adapter.HttpWebHandlerAdapter=DEBUG")
@AutoConfigureWebTestClient
@ExtendWith({SpringExtension.class, OutputCaptureExtension.class})
class GatewayAuthenticationContextTests {

    private static final DisposableServer DOWNSTREAM_SERVER = HttpServer.create()
            .port(0)
            .route(routes -> routes
                    .get("/api/v1/auth/ping", (request, response) -> response.sendString(Mono.just("public")))
                    .get("/api/books/echo-auth", (request, response) -> {
                        String authorizationHeader = request.requestHeaders().get(HttpHeaders.AUTHORIZATION);
                        return response.sendString(Mono.just(authorizationHeader == null ? "missing" : authorizationHeader));
                    }))
            .bindNow();

    private static final String DOWNSTREAM_BASE_URL = "http://localhost:" + DOWNSTREAM_SERVER.port();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void overrideRouteUris(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "user-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].uri", () -> DOWNSTREAM_BASE_URL);
        registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/api/v1/auth/**");
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
                .uri("/api/v1/auth/ping")
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
                .isUnauthorized();
    }

    @Test
    void authorizationHeaderIsForwardedUnchanged() {
        String authorizationHeader = "Bearer header.payload.signature";

        webTestClient.get()
                .uri("/api/books/echo-auth")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(authorizationHeader);
    }

    @Test
    void authorizationHeaderIsNotLoggedInPlaintext(CapturedOutput output) {
        String authorizationHeader = "Bearer sensitive-token-for-log-check";

        webTestClient.get()
                .uri("/api/books/echo-auth")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(output).contains("Completed 200 OK");
        assertThat(output).doesNotContain(authorizationHeader);
    }
}
