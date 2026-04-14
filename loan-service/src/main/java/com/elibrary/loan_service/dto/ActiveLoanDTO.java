package com.elibrary.loan_service.dto;

import com.elibrary.loan_service.domain.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Active loan summary for staff/admin views")
public class ActiveLoanDTO {

    @Schema(description = "Loan ID")
    private UUID id;

    @Schema(description = "Email address of the borrowing user")
    private String userEmail;

    @Schema(description = "Title of the borrowed book")
    private String bookTitle;

    @Schema(description = "ID of the borrowed book title")
    private Long bookId;

    @Schema(description = "Date and time the loan is due")
    private LocalDateTime dueDate;

    @Schema(description = "Current loan status", example = "ACTIVE")
    private LoanStatus status;

    @Schema(description = "Fine amount in euros", example = "0.00")
    private BigDecimal fineAmount;

    public ActiveLoanDTO() {
    }

    public ActiveLoanDTO(UUID id, String userEmail, String bookTitle, Long bookId,
                         LocalDateTime dueDate, LoanStatus status, BigDecimal fineAmount) {
        this.id = id;
        this.userEmail = userEmail;
        this.bookTitle = bookTitle;
        this.bookId = bookId;
        this.dueDate = dueDate;
        this.status = status;
        this.fineAmount = fineAmount;
    }

    public UUID getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getBookTitle() { return bookTitle; }
    public Long getBookId() { return bookId; }
    public LocalDateTime getDueDate() { return dueDate; }
    public LoanStatus getStatus() { return status; }
    public BigDecimal getFineAmount() { return fineAmount; }
}
