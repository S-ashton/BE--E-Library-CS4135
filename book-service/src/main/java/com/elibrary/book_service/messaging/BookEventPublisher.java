package com.elibrary.book_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "book.messaging.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class BookEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String addedRoutingKey;
    private final String deletedRoutingKey;

    public BookEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${book.events.exchange}") String exchange,
            @Value("${book.events.added-routing-key}") String addedRoutingKey,
            @Value("${book.events.deleted-routing-key}") String deletedRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.addedRoutingKey = addedRoutingKey;
        this.deletedRoutingKey = deletedRoutingKey;
    }

    public void publishBookAdded(BookAddedEvent event) {
        rabbitTemplate.convertAndSend(exchange, addedRoutingKey, event);
        log.info("Published book.added event for bookId={}", event.getId());
    }

    public void publishBookDeleted(BookDeletedEvent event) {
        rabbitTemplate.convertAndSend(exchange, deletedRoutingKey, event);
        log.info("Published book.deleted event for bookId={}", event.getId());
    }

}