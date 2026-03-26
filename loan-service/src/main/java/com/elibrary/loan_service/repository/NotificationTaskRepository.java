package com.elibrary.loan_service.repository;

import com.elibrary.loan_service.domain.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, UUID> {
}