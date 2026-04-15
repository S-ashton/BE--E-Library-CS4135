package com.elibrary.loan_service.service;

import com.elibrary.loan_service.client.BookCopyResponseDTO;
import com.elibrary.loan_service.client.BookServiceClient;
import com.elibrary.loan_service.client.BookTitleDTO;
import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.domain.LoanStatus;
import com.elibrary.loan_service.domain.NotificationStatus;
import com.elibrary.loan_service.domain.NotificationTask;
import com.elibrary.loan_service.domain.NotificationType;
import com.elibrary.loan_service.dto.ActiveLoanDTO;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.exception.DuplicateActiveLoanException;
import com.elibrary.loan_service.exception.LoanAlreadyReturnedException;
import com.elibrary.loan_service.exception.LoanNotFoundException;
import com.elibrary.loan_service.mapper.LoanMapper;
import com.elibrary.loan_service.messaging.LoanBorrowedEvent;
import com.elibrary.loan_service.messaging.LoanEventPublisher;
import com.elibrary.loan_service.messaging.LoanReturnedEvent;
import com.elibrary.loan_service.repository.LoanRepository;
import com.elibrary.loan_service.repository.NotificationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private static final BigDecimal DAILY_FINE_RATE = BigDecimal.valueOf(2);
    private static final int MAX_NOTIFICATION_RETRIES = 3;

    private final LoanRepository loanRepository;
    private final NotificationTaskRepository notificationTaskRepository;
    private final LoanMapper loanMapper;
    private final BookServiceClient bookServiceClient;
    private final LoanEventPublisher loanEventPublisher;
    private final EmailNotificationService emailNotificationService;

    public LoanService(
            LoanRepository loanRepository,
            NotificationTaskRepository notificationTaskRepository,
            LoanMapper loanMapper,
            BookServiceClient bookServiceClient,
            LoanEventPublisher loanEventPublisher,
            EmailNotificationService emailNotificationService
    ) {
        this.loanRepository = loanRepository;
        this.notificationTaskRepository = notificationTaskRepository;
        this.loanMapper = loanMapper;
        this.bookServiceClient = bookServiceClient;
        this.loanEventPublisher = loanEventPublisher;
        this.emailNotificationService = emailNotificationService;
    }

    @Transactional
    public LoanDTO borrowBook(Long userId, BorrowRequestDTO request, String authorization) {
        boolean hasActiveLoan = loanRepository.existsByUserIdAndBookIdAndStatus(
                userId,
                request.getBookId(),
                LoanStatus.ACTIVE
        );

        boolean hasOverdueLoan = loanRepository.existsByUserIdAndBookIdAndStatus(
                userId,
                request.getBookId(),
                LoanStatus.OVERDUE
        );

        if (hasActiveLoan || hasOverdueLoan) {
            throw new DuplicateActiveLoanException("User already has an active or overdue loan for this book");
        }

        BookCopyResponseDTO availableCopy = bookServiceClient.getAvailableCopy(
                request.getBookId(),
                authorization
        );

        Long copyId = availableCopy.getId();

        bookServiceClient.changeCopyStatus(
                copyId,
                "ON_LOAN",
                userId,
                authorization
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(14);

        Loan loan = new Loan(
                userId,
                request.getBookId(),
                copyId,
                now,
                dueDate,
                LoanStatus.ACTIVE,
                request.getEmail()
        );

        Loan savedLoan = loanRepository.save(loan);

        try {
            log.info("Attempting borrow confirmation email for loanId={} to={}", savedLoan.getId(), request.getEmail());
            emailNotificationService.sendBorrowConfirmation(request.getEmail(), savedLoan);
            log.info("Borrow confirmation email sent for loanId={}", savedLoan.getId());
        } catch (Exception ex) {
            log.warn("Borrow confirmation email failed for loanId={}", savedLoan.getId(), ex);
        }

        NotificationTask reminderTask = new NotificationTask(
                savedLoan.getId(),
                savedLoan.getUserId(),
                request.getEmail(),
                NotificationType.DUE_DATE_REMINDER,
                savedLoan.getDueDate().minusDays(1)
        );
        notificationTaskRepository.save(reminderTask);

        try {
            LoanBorrowedEvent event = new LoanBorrowedEvent(
                    savedLoan.getId(),
                    savedLoan.getUserId(),
                    savedLoan.getBookId(),
                    savedLoan.getCopyId(),
                    LocalDateTime.now()
            );
            loanEventPublisher.publishLoanBorrowed(event);
        } catch (Exception ex) {
            log.warn("loan.borrowed publish failed for loanId={}", savedLoan.getId(), ex);
        }

        return loanMapper.toDto(savedLoan);
    }

    @Transactional
    public LoanDTO returnLoan(UUID loanId, String authorization) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new LoanAlreadyReturnedException("Loan has already been returned");
        }

        LocalDateTime returnedAt = LocalDateTime.now();
        BigDecimal fineAmount = calculateFine(loan.getDueDate(), returnedAt);

        loan.markReturned(returnedAt, fineAmount);
        Loan savedLoan = loanRepository.save(loan);

        List<NotificationTask> pendingTasks =
                notificationTaskRepository.findByLoanIdAndStatus(
                        savedLoan.getId(),
                        NotificationStatus.PENDING
                );

        for (NotificationTask task : pendingTasks) {
            task.cancel();
        }
        notificationTaskRepository.saveAll(pendingTasks);

        bookServiceClient.changeCopyStatus(
                savedLoan.getCopyId(),
                "AVAILABLE",
                savedLoan.getUserId(),
                authorization
        );

        try {
            LoanReturnedEvent event = new LoanReturnedEvent(
                    savedLoan.getId(),
                    savedLoan.getUserId(),
                    savedLoan.getBookId(),
                    savedLoan.getCopyId(),
                    LocalDateTime.now()
            );
            loanEventPublisher.publishLoanReturned(event);
        } catch (Exception ex) {
            log.warn("loan.returned publish failed for loanId={}", savedLoan.getId(), ex);
        }

        return loanMapper.toDto(savedLoan);
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getLoanHistory(Long userId) {
        return loanRepository.findByUserIdOrderByBorrowDateDesc(userId)
                .stream()
                .map(loanMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActiveLoanDTO> getAllActiveLoans() {
        List<Loan> loans = loanRepository.findByStatusInOrderByBorrowDateDesc(
                List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));

        List<Long> bookIds = loans.stream().map(Loan::getBookId).distinct().toList();
        Map<Long, String> titleById = bookServiceClient.getTitlesByIds(bookIds).stream()
                .collect(java.util.stream.Collectors.toMap(BookTitleDTO::getId, BookTitleDTO::getTitle));

        return loans.stream()
                .map(loan -> new ActiveLoanDTO(
                        loan.getId(),
                        loan.getUserEmail(),
                        titleById.getOrDefault(loan.getBookId(), "Unknown"),
                        loan.getBookId(),
                        loan.getDueDate(),
                        loan.getStatus(),
                        loan.getFineAmount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasActiveLoansForBook(Long bookId) {
        return loanRepository.existsByBookIdAndStatusIn(bookId, List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));
    }

    @Transactional
    public void processOverdueLoans() {
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(
                LoanStatus.ACTIVE,
                LocalDateTime.now()
        );

        for (Loan loan : overdueLoans) {
            loan.markOverdue();
            loanRepository.save(loan);

            LocalDate today = LocalDate.now();
            LocalDateTime startOfToday = today.atStartOfDay();
            LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

            boolean overdueAlertAlreadyScheduledToday =
                    notificationTaskRepository.existsByLoanIdAndTypeAndScheduledAtBetween(
                            loan.getId(),
                            NotificationType.OVERDUE_ALERT,
                            startOfToday,
                            startOfTomorrow
                    );

            if (!overdueAlertAlreadyScheduledToday) {
                String recipientEmail = notificationTaskRepository.findTopByLoanIdOrderByScheduledAtAsc(loan.getId())
                        .map(NotificationTask::getRecipientEmail)
                        .orElse(null);

                if (recipientEmail == null || recipientEmail.isBlank()) {
                    log.warn("Skipping overdue notification for loan {} because no recipient email was found", loan.getId());
                    continue;
                }

                NotificationTask overdueTask = new NotificationTask(
                        loan.getId(),
                        loan.getUserId(),
                        recipientEmail,
                        NotificationType.OVERDUE_ALERT,
                        LocalDateTime.now()
                );

                notificationTaskRepository.save(overdueTask);
                log.info("Created overdue notification task for loan {}", loan.getId());
            }

            log.info("Marked loan {} as overdue", loan.getId());
        }
    }

    @Transactional
    public void processNotificationTasks() {
        List<NotificationTask> tasks = notificationTaskRepository.findByStatusAndScheduledAtBefore(
                NotificationStatus.PENDING,
                LocalDateTime.now()
        );

        for (NotificationTask task : tasks) {
            try {
                processNotificationTask(task);
                task.markSent();
                notificationTaskRepository.save(task);
                log.info("Processed {} notification for loan {}", task.getType(), task.getLoanId());
            } catch (Exception ex) {
                task.markFailed();

                if (task.getRetryCount() >= MAX_NOTIFICATION_RETRIES) {
                    task.markDead();
                }

                notificationTaskRepository.save(task);
                log.warn("Failed to process notification task {}", task.getId(), ex);
            }
        }
    }

    private void processNotificationTask(NotificationTask task) {
        Loan loan = loanRepository.findById(task.getLoanId())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found for notification task"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("Notifications should not be sent for returned loans");
        }

        emailNotificationService.sendNotification(task, loan);
    }

    private BigDecimal calculateFine(LocalDateTime dueDate, LocalDateTime returnedAt) {
        if (!returnedAt.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = Duration.between(dueDate, returnedAt).toDays();
        if (returnedAt.toLocalTime().isAfter(dueDate.toLocalTime()) || daysOverdue == 0) {
            daysOverdue += 1;
        }

        return DAILY_FINE_RATE.multiply(BigDecimal.valueOf(daysOverdue));
    }
}