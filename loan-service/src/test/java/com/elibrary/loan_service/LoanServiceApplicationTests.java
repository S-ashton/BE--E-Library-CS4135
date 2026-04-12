package com.elibrary.loan_service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "loan.events.exchange=loan.events",
        "loan.events.borrowed-routing-key=loan.borrowed",
        "loan.events.returned-routing-key=loan.returned"
})
class LoanServiceApplicationTests {

    @MockitoBean
    private AmqpAdmin amqpAdmin;

    @Test
    void contextLoads() {
    }
}