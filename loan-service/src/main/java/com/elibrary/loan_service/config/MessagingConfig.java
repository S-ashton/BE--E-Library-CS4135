package com.elibrary.loan_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    private static final Logger log = LoggerFactory.getLogger(MessagingConfig.class);

    @Bean
    public TopicExchange loanEventsExchange() {
        return new TopicExchange("loan.events", true, false);
    }

    @Bean
    public Queue loanTestQueue() {
        return new Queue("loan.test.queue", true);
    }

    @Bean
    public Binding borrowedBinding(Queue loanTestQueue, TopicExchange loanEventsExchange) {
        return BindingBuilder.bind(loanTestQueue).to(loanEventsExchange).with("loan.borrowed");
    }

    @Bean
    public Binding returnedBinding(Queue loanTestQueue, TopicExchange loanEventsExchange) {
        return BindingBuilder.bind(loanTestQueue).to(loanEventsExchange).with("loan.returned");
    }

    @Bean
    public CommandLineRunner declareRabbitResources(
            AmqpAdmin amqpAdmin,
            TopicExchange loanEventsExchange,
            Queue loanTestQueue,
            Binding borrowedBinding,
            Binding returnedBinding
    ) {
        return args -> {
            log.info("Declaring RabbitMQ exchange, queue, and bindings...");
            amqpAdmin.declareExchange(loanEventsExchange);
            amqpAdmin.declareQueue(loanTestQueue);
            amqpAdmin.declareBinding(borrowedBinding);
            amqpAdmin.declareBinding(returnedBinding);
            log.info("RabbitMQ declarations completed.");
        };
    }
}