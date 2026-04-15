package com.elibrary.loan_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "loan.messaging.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MessagingConfig {

    @Bean
    public TopicExchange loanEventsExchange() {
        return new TopicExchange("loan.events", true, false);
    }

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange("user.events", true, false);
    }

    @Bean
    public Queue userDeletedQueue(@Value("${user.events.deleted-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue userEmailUpdatedQueue(@Value("${user.events.email-updated-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding userDeletedBinding(
            Queue userDeletedQueue,
            TopicExchange userEventsExchange,
            @Value("${user.events.deleted-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(userDeletedQueue).to(userEventsExchange).with(routingKey);
    }

    @Bean
    public Binding userEmailUpdatedBinding(
            Queue userEmailUpdatedQueue,
            TopicExchange userEventsExchange,
            @Value("${user.events.email-updated-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(userEmailUpdatedQueue).to(userEventsExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public CommandLineRunner declareLoanExchange(
            AmqpAdmin amqpAdmin,
            TopicExchange loanEventsExchange,
            TopicExchange userEventsExchange,
            Queue userDeletedQueue,
            Queue userEmailUpdatedQueue,
            Binding userDeletedBinding,
            Binding userEmailUpdatedBinding
    ) {
        return args -> {
            amqpAdmin.declareExchange(loanEventsExchange);
            amqpAdmin.declareExchange(userEventsExchange);
            amqpAdmin.declareQueue(userDeletedQueue);
            amqpAdmin.declareQueue(userEmailUpdatedQueue);
            amqpAdmin.declareBinding(userDeletedBinding);
            amqpAdmin.declareBinding(userEmailUpdatedBinding);
        };
    }
}
