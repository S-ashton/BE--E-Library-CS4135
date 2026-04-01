package com.elibrary.loan_service.repository;

import com.elibrary.loan_service.domain.NotificationStatus;
import com.elibrary.loan_service.domain.NotificationTask;
import com.elibrary.loan_service.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, UUID> {

    List<NotificationTask> findByLoanIdAndStatus(UUID loanId, NotificationStatus status);

    boolean existsByLoanIdAndTypeAndStatus(UUID loanId, NotificationType type, NotificationStatus status);
}