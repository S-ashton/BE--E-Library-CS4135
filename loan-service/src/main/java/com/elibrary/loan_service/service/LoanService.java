package com.elibrary.loan_service.service;

import com.elibrary.loan_service.client.BookServiceClient;
import com.elibrary.loan_service.domain.*;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.exception.DuplicateActiveLoanException;
import com.elibrary.loan_service.mapper.LoanMapper;
import com.elibrary.loan_service.messaging.LoanBorrowedEvent;
import com.elibrary.loan_service.messaging.LoanEventPublisher;
import com.elibrary.loan_service.repository.LoanRepository;
import com.elibrary.loan_service.repository.NotificationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final NotificationTaskRepository notificationTaskRepository;
    private final LoanMapper loanMapper;
    private final BookServiceClient bookServiceClient;
    private final LoanEventPublisher loanEventPublisher;

    public LoanService(LoanRepository loanRepository,
                       NotificationTaskRepository notificationTaskRepository,
                       LoanMapper loanMapper,
                       BookServiceClient bookServiceClient,
                       LoanEventPublisher loanEventPublisher) {
        this.loanRepository = loanRepository;
        this.notificationTaskRepository = notificationTaskRepository;
        this.loanMapper = loanMapper;
        this.bookServiceClient = bookServiceClient;
        this.loanEventPublisher = loanEventPublisher;
    }

    @Transactional
    public LoanDTO borrowBook(UUID userId, BorrowRequestDTO request) {
        boolean duplicateExists = loanRepository.existsByUserIdAndBookIdAndStatus(
                userId,
                request.getBookId(),
                LoanStatus.ACTIVE
        );

        if (duplicateExists) {
            throw new DuplicateActiveLoanException("User already has an active loan for this book");
        }

        // Synchronous Book Service call
        bookServiceClient.reserveBook(request.getBookId(), userId);

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

        NotificationTask reminderTask = new NotificationTask(
                savedLoan.getId(),
                savedLoan.getUserId(),
                NotificationType.DUE_DATE_REMINDER,
                savedLoan.getDueDate().minusDays(1)
        );
        notificationTaskRepository.save(reminderTask);

        try {
            LoanBorrowedEvent event = new LoanBorrowedEvent(
                    savedLoan.getId(),
                    savedLoan.getUserId(),
                    savedLoan.getBookId(),
                    LocalDateTime.now()
            );
            loanEventPublisher.publishLoanBorrowed(event);
        } catch (Exception ex) {
            log.warn("loan.borrowed event publish failed for loanId={}", savedLoan.getId(), ex);
        }

        return loanMapper.toDto(savedLoan);
    }
}