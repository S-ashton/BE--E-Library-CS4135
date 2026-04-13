package com.elibrary.recommendation_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Value("${loan.events.exchange}")
    private String loanExchange;

    @Value("${loan.events.queue}")
    private String loanQueue;

    @Value("${loan.events.borrowed-routing-key}")
    private String borrowedRoutingKey;

    @Value("${book.events.exchange}")
    private String bookExchange;

    @Value("${book.events.queue}")
    private String bookQueue;

    @Value("${book.events.added-routing-key}")
    private String bookAddedRoutingKey;

    @Bean
    public Queue loanEventQueue() {
        return new Queue(loanQueue, true);
    }

    @Bean
    public TopicExchange loanEventsExchange() {
        return new TopicExchange(loanExchange, true, false);
    }

    @Bean
    public Binding loanEventBinding() {
        return BindingBuilder.bind(loanEventQueue())
                .to(loanEventsExchange())
                .with(borrowedRoutingKey);
    }

    @Bean
    public Queue bookEventQueue() {
        return new Queue(bookQueue, true);
    }

    @Bean
    public TopicExchange bookEventsExchange() {
        return new TopicExchange(bookExchange, true, false);
    }

    @Bean
    public Binding bookEventBinding() {
        return BindingBuilder.bind(bookEventQueue())
                .to(bookEventsExchange())
                .with(bookAddedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
