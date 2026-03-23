package com.elibrary.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayApplicationTests {

	@Autowired
	private RouteLocator routeLocator;

	@Autowired
	private RouteDefinitionLocator routeDefinitionLocator;

	@Autowired
	private WebTestClient webTestClient;

	@LocalServerPort
	private int port;

	@Test
	void contextLoads() {
	}

	@Test
	void allExpectedRoutesAreRegistered() {
		List<String> routeIds = routeLocator.getRoutes()
				.map(route -> route.getId())
				.collectList()
				.block();

		assertThat(routeIds).contains(
				"user-service",
				"book-service",
				"loan-service",
				"recommendation-service"
		);
	}

	@Test
	void fourRoutesConfigured() {
		long count = routeLocator.getRoutes().count().block();
		assertThat(count).isEqualTo(4);
	}

	@Test
	void routeDefinitionsUseExpectedPathPredicates() {
		Map<String, List<String>> pathPredicatesByRouteId = routeDefinitionLocator.getRouteDefinitions()
				.collectList()
				.block()
				.stream()
				.collect(Collectors.toMap(
						RouteDefinition::getId,
						route -> route.getPredicates().stream()
								.flatMap(predicate -> predicate.getArgs().values().stream())
								.map(String::valueOf)
								.toList()
				));

		assertThat(pathPredicatesByRouteId.get("user-service"))
				.containsExactlyInAnyOrder("/api/v1/auth/**", "/api/users/**");
		assertThat(pathPredicatesByRouteId.get("book-service"))
				.containsExactly("/api/books/**");
		assertThat(pathPredicatesByRouteId.get("loan-service"))
				.containsExactly("/api/loans/**");
		assertThat(pathPredicatesByRouteId.get("recommendation-service"))
				.containsExactly("/api/recommendations/**");
	}

	@Test
	void invalidRoutesReturn404() {
		webTestClient.get()
				.uri("/api/unknown/test")
				.exchange()
				.expectStatus()
				.isNotFound();
	}

	@Test
	void allowedOriginPreflightSucceeds() {
		webTestClient.options()
				.uri(gatewayUrl("/api/books/test"))
				.header("Origin", "http://localhost:3000")
				.header("Access-Control-Request-Method", "GET")
				.exchange()
				.expectStatus()
				.isOk()
				.expectHeader()
				.valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
	}

	@Test
	void disallowedOriginPreflightIsRejected() {
		webTestClient.options()
				.uri(gatewayUrl("/api/books/test"))
				.header("Origin", "http://malicious.example")
				.header("Access-Control-Request-Method", "GET")
				.exchange()
				.expectStatus()
				.isForbidden();
	}

	private String gatewayUrl(String path) {
		return "http://localhost:" + port + path;
	}
}
