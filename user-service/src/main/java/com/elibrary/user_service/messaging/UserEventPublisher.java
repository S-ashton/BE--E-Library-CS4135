package com.elibrary.user_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final RabbitTemplate userRabbitTemplate;
    private final String exchange;
    private final String deletedRoutingKey;
    private final String emailUpdatedRoutingKey;

    public UserEventPublisher(
            RabbitTemplate userRabbitTemplate,
            @Value("${user.events.exchange}") String exchange,
            @Value("${user.events.deleted-routing-key}") String deletedRoutingKey,
            @Value("${user.events.email-updated-routing-key}") String emailUpdatedRoutingKey
    ) {
        this.userRabbitTemplate = userRabbitTemplate;
        this.exchange = exchange;
        this.deletedRoutingKey = deletedRoutingKey;
        this.emailUpdatedRoutingKey = emailUpdatedRoutingKey;
    }

    public void publishUserDeleted(UserDeletedEvent event) {
        userRabbitTemplate.convertAndSend(exchange, deletedRoutingKey, event);
        log.info("Published user.deleted event for userId={}", event.getUserId());
    }

    public void publishUserEmailUpdated(UserEmailUpdatedEvent event) {
        userRabbitTemplate.convertAndSend(exchange, emailUpdatedRoutingKey, event);
        log.info("Published user.email.updated event for userId={}", event.getUserId());
    }
}
