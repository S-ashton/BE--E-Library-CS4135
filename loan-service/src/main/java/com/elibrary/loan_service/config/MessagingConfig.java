package com.elibrary.loan_service.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public TopicExchange loanEventsExchange(@Value("${loan.events.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }
}