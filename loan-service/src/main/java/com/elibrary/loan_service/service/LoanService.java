package com.elibrary.loan_service.service;

import com.elibrary.loan_service.client.BookServiceClient;
import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.domain.LoanStatus;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.exception.BookUnavailableException;
import com.elibrary.loan_service.exception.DuplicateActiveLoanException;
import com.elibrary.loan_service.mapper.LoanMapper;
import com.elibrary.loan_service.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final BookServiceClient bookServiceClient;

    public LoanService(LoanRepository loanRepository, LoanMapper loanMapper, BookServiceClient bookServiceClient) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.bookServiceClient = bookServiceClient;
    }

    public LoanDTO borrowBook(UUID userId, BorrowRequestDTO request) {
        boolean available = bookServiceClient.isBookAvailable(request.getBookId());
        if (!available) {
            throw new BookUnavailableException("Book is unavailable");
        }

        boolean duplicateExists = loanRepository.existsByUserIdAndBookIdAndStatus(
                userId,
                request.getBookId(),
                LoanStatus.ACTIVE
        );

        if (duplicateExists) {
            throw new DuplicateActiveLoanException("User already has an active loan for this book");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(14);

        Loan loan = new Loan(
                userId,
                request.getBookId(),
                now,
                dueDate,
                LoanStatus.ACTIVE
        );

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toDto(savedLoan);
    }
}