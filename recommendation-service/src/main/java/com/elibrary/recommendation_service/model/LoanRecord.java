package com.elibrary.recommendation_service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Loan event used for collaborative filtering")
public class LoanRecord {

    @Schema(description = "Unique ID of the loan event", example = "10045")
    private Long loanId;

    @Schema(description = "ID of the user who borrowed the book", example = "12")
    private Long userId;

    @Schema(description = "ID of the borrowed book", example = "39")
    private Long bookId;

    @Schema(description = "Timestamp of when the loan occurred (ISO-8601)",
            example = "2025-02-14T10:23:54Z")
    private String timestamp;

    public LoanRecord() {}

    public LoanRecord(Long loanId, Long userId, Long bookId, String timestamp) {
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
        this.timestamp = timestamp;
    }

    public Long getLoanId() { return loanId; }
    public Long getUserId() { return userId; }
    public Long getBookId() { return bookId; }
    public String getTimestamp() { return timestamp; }

    public void setLoanId(Long loanId) { this.loanId = loanId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
