package com.elibrary.loan_service.domain;

import com.elibrary.loan_service.exception.LoanAlreadyReturnedException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private Long copyId;

    @Column(nullable = false)
    private LocalDateTime borrowDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(nullable = false)
    private BigDecimal fineAmount;

    @Column
    private String userEmail;

    public Loan() {
        this.fineAmount = BigDecimal.ZERO;
    }

    public Loan(Long userId, Long bookId, Long copyId,
                LocalDateTime borrowDate, LocalDateTime dueDate, LoanStatus status) {
        this.userId = userId;
        this.bookId = bookId;
        this.copyId = copyId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.fineAmount = BigDecimal.ZERO;
    }

    public Loan(Long userId, Long bookId, Long copyId,
                LocalDateTime borrowDate, LocalDateTime dueDate, LoanStatus status, String userEmail) {
        this.userId = userId;
        this.bookId = bookId;
        this.copyId = copyId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.fineAmount = BigDecimal.ZERO;
        this.userEmail = userEmail;
    }

    public void markReturned(LocalDateTime returnedAt, BigDecimal fineAmount) {
        if (this.status == LoanStatus.RETURNED) {
            throw new LoanAlreadyReturnedException("Loan is already returned");
        }

        this.returnDate = returnedAt;
        this.status = LoanStatus.RETURNED;
        this.fineAmount = fineAmount.max(BigDecimal.ZERO);
    }

    public void markOverdue() {
        if (this.status == LoanStatus.ACTIVE) {
            this.status = LoanStatus.OVERDUE;
        }
    }

    public UUID getId() {
        return id;
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

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}