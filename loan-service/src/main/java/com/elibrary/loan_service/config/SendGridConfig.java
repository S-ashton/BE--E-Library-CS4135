package com.elibrary.loan_service.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "loan.email.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SendGridConfig {

    @Bean
    public SendGrid sendGrid(@Value("${sendgrid.api-key}") String apiKey) {
        return new SendGrid(apiKey);
    }
}