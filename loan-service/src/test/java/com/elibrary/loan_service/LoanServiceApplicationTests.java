package com.elibrary.loan_service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class LoanServiceApplicationTests {

	@MockBean
	ConnectionFactory connectionFactory;

	@MockBean
	AmqpAdmin amqpAdmin;

	@Test
	void contextLoads() {
	}

}
