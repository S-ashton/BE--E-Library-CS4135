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
    private String exchange;

    @Value("${loan.events.queue}")
    private String queue;

    @Value("${loan.events.borrowed-routing-key}")
    private String borrowedRoutingKey;

    @Bean
    public Queue loanEventQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public TopicExchange loanEventsExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Binding loanEventBinding() {
        return BindingBuilder.bind(loanEventQueue())
                .to(loanEventsExchange())
                .with(borrowedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
