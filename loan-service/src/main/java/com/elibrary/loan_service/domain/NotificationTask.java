package com.elibrary.loan_service.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_tasks")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID loanId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private int retryCount;

    public NotificationTask() {
    }

    public NotificationTask(UUID loanId, Long userId, NotificationType type, LocalDateTime scheduledAt) {
        this.loanId = loanId;
        this.userId = userId;
        this.type = type;
        this.scheduledAt = scheduledAt;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
    }

    public void cancel() {
        if (this.status == NotificationStatus.PENDING) {
            this.status = NotificationStatus.CANCELLED;
        }
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        if (this.status == NotificationStatus.PENDING || this.status == NotificationStatus.FAILED) {
            this.status = NotificationStatus.FAILED;
            this.retryCount += 1;
        }
    }

    public void markDead() {
        this.status = NotificationStatus.DEAD;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLoanId() {
        return loanId;
    }

    public Long getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }
}