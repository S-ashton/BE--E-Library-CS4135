package com.elibrary.loan_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoanEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoanEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String borrowedRoutingKey;

    public LoanEventPublisher(RabbitTemplate rabbitTemplate,
                              @Value("${loan.events.exchange}") String exchange,
                              @Value("${loan.events.borrowed-routing-key}") String borrowedRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.borrowedRoutingKey = borrowedRoutingKey;
    }

    public void publishLoanBorrowed(LoanBorrowedEvent event) {
        rabbitTemplate.convertAndSend(exchange, borrowedRoutingKey, event);
        log.info("Published loan.borrowed event for loanId={}", event.getLoanId());
    }
}