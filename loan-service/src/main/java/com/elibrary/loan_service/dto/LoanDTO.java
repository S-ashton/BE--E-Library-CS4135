package com.elibrary.loan_service.dto;

import com.elibrary.loan_service.domain.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Loan response payload")
public class LoanDTO {

    @Schema(description = "Loan ID", example = "1e1a6183-7aa7-4d48-a868-9a39e613370d")
    private UUID id;

    @Schema(description = "Authenticated user ID who owns the loan", example = "1")
    private Long userId;

    @Schema(description = "Borrowed book ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID bookId;

    @Schema(description = "Date and time the book was borrowed")
    private LocalDateTime borrowDate;

    @Schema(description = "Date and time the loan is due")
    private LocalDateTime dueDate;

    @Schema(description = "Date and time the book was returned, if returned")
    private LocalDateTime returnDate;

    @Schema(description = "Current loan status", example = "ACTIVE")
    private LoanStatus status;

    @Schema(description = "Fine amount in euros", example = "0")
    private BigDecimal fineAmount;

    public LoanDTO() {
    }

    public LoanDTO(UUID id, Long userId, UUID bookId, LocalDateTime borrowDate,
                   LocalDateTime dueDate, LocalDateTime returnDate,
                   LoanStatus status, BigDecimal fineAmount) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fineAmount = fineAmount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }
}