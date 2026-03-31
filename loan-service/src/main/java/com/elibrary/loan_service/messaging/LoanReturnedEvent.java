package com.elibrary.loan_service.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanReturnedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID loanId;
    private Long userId;
    private UUID bookId;
    private LocalDateTime timestamp;

    public LoanReturnedEvent() {
    }

    public LoanReturnedEvent(UUID loanId, Long userId, UUID bookId, LocalDateTime timestamp) {
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
        this.timestamp = timestamp;
    }

    public UUID getLoanId() {
        return loanId;
    }

    public void setLoanId(UUID loanId) {
        this.loanId = loanId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}