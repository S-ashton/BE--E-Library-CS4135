package com.elibrary.book_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.elibrary.book_service.model.Status;
import com.elibrary.book_service.service.BookService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoanEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoanEventConsumer.class);

    private BookService bookService;

    public LoanEventConsumer(BookService bookService){
        this.bookService = bookService;
    }

   @RabbitListener(queues = "loan.events")
    public void handleBorrowed(LoanBorrowedEvent event,
                            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("Loan borrowed event received, routing key: {}", routingKey);
        Long copyId = event.getCopyId();
        bookService.changeStatus(copyId, Status.ON_LOAN);
    }

    @RabbitListener(queues = "loan.events")
    public void handleReturned(LoanReturnedEvent event,
                            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("Loan borrowed event received, routing key: {}", routingKey);
        Long copyId = event.getCopyId();
        bookService.changeStatus(copyId, Status.AVAILABLE);
    }
}
