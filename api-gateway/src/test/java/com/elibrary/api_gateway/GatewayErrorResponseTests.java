package com.elibrary.api_gateway;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.name=gateway-error-test")
@AutoConfigureWebTestClient
class GatewayErrorResponseTests {

    private static final int UNAVAILABLE_PORT = findUnusedPort();

    private static final DisposableServer SLOW_SERVER = HttpServer.create()
            .port(0)
            .route(routes -> routes.get("/api/recommendations/slow", (request, response) -> response
                    .sendString(Mono.delay(Duration.ofMillis(300)).thenReturn("slow"))))
            .bindNow();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void registerPorts(DynamicPropertyRegistry registry) {
        registry.add("test.downstream.unavailable-port", () -> UNAVAILABLE_PORT);
        registry.add("test.downstream.slow-port", SLOW_SERVER::port);
    }

    @AfterAll
    static void stopServer() {
        SLOW_SERVER.disposeNow();
    }

    @Test
    void downstreamConnectionFailuresReturnStandardBadGatewayError() {
        webTestClient.get()
                .uri("/api/books/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .exchange()
                .expectStatus()
                .isEqualTo(502)
                .expectHeader()
                .contentTypeCompatibleWith("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(502)
                .jsonPath("$.error").isEqualTo("Bad Gateway")
                .jsonPath("$.message").isEqualTo("Downstream service unavailable")
                .jsonPath("$.path").isEqualTo("/api/books/test")
                .jsonPath("$.requestId").exists()
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void downstreamTimeoutsReturnStandardGatewayTimeoutError() {
        webTestClient.get()
                .uri("/api/recommendations/slow")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .exchange()
                .expectStatus()
                .isEqualTo(504)
                .expectHeader()
                .contentTypeCompatibleWith("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(504)
                .jsonPath("$.error").isEqualTo("Gateway Timeout")
                .jsonPath("$.message").isEqualTo("Downstream service timed out")
                .jsonPath("$.path").isEqualTo("/api/recommendations/slow")
                .jsonPath("$.requestId").exists()
                .jsonPath("$.timestamp").exists();
    }

    private static int findUnusedPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
        catch (IOException exception) {
            throw new IllegalStateException("Unable to find an unused port for gateway tests", exception);
        }
    }
}
