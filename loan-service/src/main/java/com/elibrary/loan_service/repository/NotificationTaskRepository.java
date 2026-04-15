package com.elibrary.loan_service.repository;

import com.elibrary.loan_service.domain.NotificationStatus;
import com.elibrary.loan_service.domain.NotificationTask;
import com.elibrary.loan_service.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, UUID> {

    List<NotificationTask> findByStatusAndScheduledAtBefore(
            NotificationStatus status,
            LocalDateTime scheduledAt
    );

    List<NotificationTask> findByLoanIdAndStatus(
            UUID loanId,
            NotificationStatus status
    );

    boolean existsByLoanIdAndTypeAndStatus(
            UUID loanId,
            NotificationType type,
            NotificationStatus status
    );

    boolean existsByLoanIdAndTypeAndScheduledAtBetween(
            UUID loanId,
            NotificationType type,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<NotificationTask> findTopByLoanIdOrderByScheduledAtAsc(UUID loanId);

    void deleteByLoanIdIn(List<UUID> loanIds);
}
