package com.elibrary.book_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoanEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoanEventConsumer.class);

    @RabbitListener(queues = "loan.events", condition = "headers['amqp_receivedRoutingKey'] == 'loan.borrowed'")
    public void handleLoanEvent(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("Received message with routing key: {}", routingKey);

        switch (routingKey) {
            case "loan.borrowed" -> handleBorrowed(message);
            case "loan.returned" -> handleReturned(message);
            default -> log.warn("Unknown routing key: {}", routingKey);
        }
    }

    private void handleBorrowed(Message message) {
        // deserialize and handle
    }

    private void handleReturned(Message message) {
        // deserialize and handle
    }
}
