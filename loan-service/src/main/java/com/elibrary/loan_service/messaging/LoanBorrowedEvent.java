package com.elibrary.loan_service.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public class LoanBorrowedEvent {

    private UUID loanId;
    private UUID userId;
    private UUID bookId;
    private LocalDateTime timestamp;

    public LoanBorrowedEvent() {
    }

    public LoanBorrowedEvent(UUID loanId, UUID userId, UUID bookId, LocalDateTime timestamp) {
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
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