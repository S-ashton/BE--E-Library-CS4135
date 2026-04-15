package com.elibrary.book_service.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Value("${loan.events.exchange}")
    private String loanExchange;

    @Value("${loan.events.borrowed-routing-key}")
    private String borrowedRoutingKey;

    @Value("${loan.events.returned-routing-key}")
    private String returnedRoutingKey;

    @Value("${loan.events.borrowed-queue}")
    private String borrowedQueue;

    @Value("${loan.events.returned-queue}")
    private String returnedQueue;

    @Bean
    public TopicExchange loanEventsExchange() {
        return new TopicExchange(loanExchange, true, false);
    }

    @Bean
    public TopicExchange bookEventsExchange(){
        return new TopicExchange("book.events", true, false);
    }

    @Bean
    public Queue bookDeletedFanoutQueue() {
        return new Queue("book-service.book.deleted", true);
    }

    @Bean
    public Binding bookDeletedFanoutBinding() {
        return BindingBuilder.bind(bookDeletedFanoutQueue())
                .to(bookEventsExchange())
                .with("book.deleted");
    }

    @Bean
    public Queue loanBorrowedQueue() {
        return new Queue(borrowedQueue, true);
    }

    @Bean
    public Queue loanReturnedQueue() {
        return new Queue(returnedQueue, true);
    }

    @Bean
    public Binding loanBorrowedBinding() {
        return BindingBuilder.bind(loanBorrowedQueue())
                .to(loanEventsExchange())
                .with(borrowedRoutingKey);
    }

    @Bean
    public Binding loanReturnedBinding() {
        return BindingBuilder.bind(loanReturnedQueue())
                .to(loanEventsExchange())
                .with(returnedRoutingKey);
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
            TopicExchange loanEventsExchange
    ) {
        return args -> amqpAdmin.declareExchange(loanEventsExchange);
    }

    @Bean 
    public CommandLineRunner declareBookExchange(
            AmqpAdmin amqpAdmin,
            TopicExchange bookEventsExchange
    ) {
        return args -> amqpAdmin.declareExchange(bookEventsExchange);
    }
}