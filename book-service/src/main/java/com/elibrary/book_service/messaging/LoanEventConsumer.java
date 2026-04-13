package com.elibrary.book_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoanEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoanEventConsumer.class);

   @RabbitListener(queues = "loan.events")
    public void handleBorrowed(LoanBorrowedEvent event,
                            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("Loan borrowed event received, routing key: {}", routingKey);
        // use event directly
    }

    @RabbitListener(queues = "loan.events")
    public void handleBorrowed(LoanReturnedEvent event,
                            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("Loan borrowed event received, routing key: {}", routingKey);
        // use event directly
    }
}
