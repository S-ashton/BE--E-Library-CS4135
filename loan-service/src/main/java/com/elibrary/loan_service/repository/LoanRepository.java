package com.elibrary.loan_service.repository;

import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.domain.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByUserIdAndBookIdAndStatus(Long userId, UUID bookId, LoanStatus status);
}