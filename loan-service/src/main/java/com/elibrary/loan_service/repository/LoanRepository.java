package com.elibrary.loan_service.repository;

import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.domain.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, LoanStatus status);

    List<Loan> findByUserIdOrderByBorrowDateDesc(Long userId);

    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDateTime dateTime);

    List<Loan> findByStatusOrderByBorrowDateDesc(LoanStatus status);

    List<Loan> findByStatusInOrderByBorrowDateDesc(List<LoanStatus> statuses);

    boolean existsByBookIdAndStatusIn(Long bookId, List<LoanStatus> statuses);

    List<Loan> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Loan l SET l.userEmail = :newEmail WHERE l.userId = :userId")
    void updateUserEmailByUserId(Long userId, String newEmail);

    void deleteByUserId(Long userId);
}
