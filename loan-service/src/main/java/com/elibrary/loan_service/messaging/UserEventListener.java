package com.elibrary.loan_service.messaging;

import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.repository.LoanRepository;
import com.elibrary.loan_service.repository.NotificationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    private final LoanRepository loanRepository;
    private final NotificationTaskRepository notificationTaskRepository;

    public UserEventListener(
            LoanRepository loanRepository,
            NotificationTaskRepository notificationTaskRepository
    ) {
        this.loanRepository = loanRepository;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @RabbitListener(queues = "${user.events.deleted-queue}")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        if (event.getUserId() == null) {
            log.warn("Received UserDeletedEvent with null userId — skipping");
            return;
        }
        log.info("Received user.deleted event for userId={}", event.getUserId());

        List<Loan> loans = loanRepository.findByUserId(event.getUserId());
        List<UUID> loanIds = loans.stream().map(Loan::getId).toList();

        if (!loanIds.isEmpty()) {
            notificationTaskRepository.deleteByLoanIdIn(loanIds);
            loanRepository.deleteByUserId(event.getUserId());
            log.info("Deleted {} loan(s) and associated notification tasks for userId={}", loanIds.size(), event.getUserId());
        } else {
            log.info("No loans found for deleted userId={}", event.getUserId());
        }
    }

    @RabbitListener(queues = "${user.events.email-updated-queue}")
    @Transactional
    public void handleUserEmailUpdated(UserEmailUpdatedEvent event) {
        if (event.getUserId() == null || event.getNewEmail() == null) {
            log.warn("Received UserEmailUpdatedEvent with missing fields — skipping");
            return;
        }
        log.info("Received user.email.updated event for userId={}", event.getUserId());

        loanRepository.updateUserEmailByUserId(event.getUserId(), event.getNewEmail());
        log.info("Updated userEmail on loans for userId={} to {}", event.getUserId(), event.getNewEmail());
    }
}
