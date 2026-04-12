package com.elibrary.recommendation_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Loan event published by loan-service")
public class LoanRecord {

    @Schema(description = "Unique ID of the loan event",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID loanId;

    @Schema(description = "ID of the user who borrowed the book",
            example = "550e8400-e29b-41d4-a716-446655440111")
    private UUID userId;

    @Schema(description = "ID of the borrowed book",
            example = "550e8400-e29b-41d4-a716-446655440222")
    private UUID bookId;

    @Schema(description = "Timestamp of the loan event (ISO-8601)",
            example = "2025-02-14T10:23:54")
    private LocalDateTime timestamp;

    public LoanRecord() {}

    public LoanRecord(UUID loanId, UUID userId, UUID bookId, LocalDateTime timestamp) {
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
        this.timestamp = timestamp;
    }

    public UUID getLoanId() { return loanId; }
    public UUID getUserId() { return userId; }
    public UUID getBookId() { return bookId; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setLoanId(UUID loanId) { this.loanId = loanId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

