package com.elibrary.book_service.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public class LoanBorrowedEvent {

    private UUID loanId;
    private Long userId;
    private Long bookId;
    private Long copyId;
    private LocalDateTime timestamp;

    public LoanBorrowedEvent() {
    }

    public LoanBorrowedEvent(UUID loanId, Long userId, Long bookId, Long copyId, LocalDateTime timestamp) {
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
        this.copyId = copyId;
        this.timestamp = timestamp;
    }

    public UUID getLoanId() {
        return loanId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public Long getCopyId() {
        return copyId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}