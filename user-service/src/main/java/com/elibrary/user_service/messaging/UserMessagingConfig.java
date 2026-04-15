package com.elibrary.user_service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "user.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class UserMessagingConfig {

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange("user.events", true, false);
    }

    @Bean
    public MessageConverter userJsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate userRabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter userJsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(userJsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public CommandLineRunner declareUserExchange(
            AmqpAdmin amqpAdmin,
            TopicExchange userEventsExchange
    ) {
        return args -> amqpAdmin.declareExchange(userEventsExchange);
    }
}
