package com.elibrary.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies that the gateway context loads and all expected routes are registered.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

	@Autowired
	private RouteLocator routeLocator;

	@Test
	void contextLoads() {
		// Application context starts without errors, config-server and Eureka are disabled in test profile.
	}

	@Test
	void allExpectedRoutesAreRegistered() {
		List<String> routeIds = routeLocator.getRoutes()
				.map(route -> route.getId())
				.collectList()
				.block();

		// Every downstream service must have a corresponding gateway route.
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
		// Exactly four services are routed only.
		assertThat(count).isEqualTo(4);
	}
}
