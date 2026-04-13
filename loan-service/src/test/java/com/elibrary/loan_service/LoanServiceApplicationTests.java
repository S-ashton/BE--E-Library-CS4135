package com.elibrary.loan_service;

import com.elibrary.loan_service.client.BookServiceClient;
import com.elibrary.loan_service.mapper.LoanMapper;
import com.elibrary.loan_service.messaging.LoanEventPublisher;
import com.elibrary.loan_service.repository.LoanRepository;
import com.elibrary.loan_service.repository.NotificationTaskRepository;
import com.elibrary.loan_service.service.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "loan.messaging.enabled=false",
        "loan.email.enabled=false",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "loan.events.exchange=loan.events",
        "loan.events.borrowed-routing-key=loan.borrowed",
        "loan.events.returned-routing-key=loan.returned"
})
class LoanServiceApplicationTests {

    @MockitoBean
    private LoanRepository loanRepository;

    @MockitoBean
    private NotificationTaskRepository notificationTaskRepository;

    @MockitoBean
    private BookServiceClient bookServiceClient;

    @MockitoBean
    private LoanEventPublisher loanEventPublisher;

    @MockitoBean
    private EmailNotificationService emailNotificationService;

    @MockitoBean
    private LoanMapper loanMapper;

    @Test
    void contextLoads() {
    }
}