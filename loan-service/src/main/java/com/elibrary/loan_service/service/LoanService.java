package com.elibrary.loan_service.service;

import com.elibrary.loan_service.client.BookServiceClient;
import com.elibrary.loan_service.domain.*;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.exception.DuplicateActiveLoanException;
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
import java.time.LocalDateTime;
import java.util.List;
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
    public LoanDTO borrowBook(Long userId, BorrowRequestDTO request) {
        boolean duplicateExists = loanRepository.existsByUserIdAndBookIdAndStatus(
                userId,
                request.getBookId(),
                LoanStatus.ACTIVE
        );

        if (duplicateExists) {
            throw new DuplicateActiveLoanException("User already has an active loan for this book");
        }

        bookServiceClient.reserveBook(request.getBookId(), userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(14);

        Loan loan = new Loan(userId, request.getBookId(), now, dueDate, LoanStatus.ACTIVE);
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
            log.warn("loan.borrowed publish failed for loanId={}", savedLoan.getId(), ex);
        }

        return loanMapper.toDto(savedLoan);
    }

    @Transactional
    public LoanDTO returnLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        LocalDateTime returnedAt = LocalDateTime.now();
        BigDecimal fineAmount = calculateFine(loan.getDueDate(), returnedAt);

        loan.markReturned(returnedAt, fineAmount);
        Loan savedLoan = loanRepository.save(loan);

        List<NotificationTask> pendingTasks =
                notificationTaskRepository.findByLoanIdAndStatus(savedLoan.getId(), NotificationStatus.PENDING);

        for (NotificationTask task : pendingTasks) {
            task.cancel();
        }
        notificationTaskRepository.saveAll(pendingTasks);

        bookServiceClient.returnBook(savedLoan.getBookId(), savedLoan.getUserId());

        try {
            LoanReturnedEvent event = new LoanReturnedEvent(
                    savedLoan.getId(),
                    savedLoan.getUserId(),
                    savedLoan.getBookId(),
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

    @Transactional
    public void processOverdueLoans() {
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(
                LoanStatus.ACTIVE,
                LocalDateTime.now()
        );

        for (Loan loan : overdueLoans) {
            loan.markOverdue();
            loanRepository.save(loan);

            boolean overdueAlertExists = notificationTaskRepository.existsByLoanIdAndTypeAndStatus(
                    loan.getId(),
                    NotificationType.OVERDUE_ALERT,
                    NotificationStatus.PENDING
            );

            if (!overdueAlertExists) {
                NotificationTask overdueTask = new NotificationTask(
                        loan.getId(),
                        loan.getUserId(),
                        NotificationType.OVERDUE_ALERT,
                        LocalDateTime.now()
                );
                notificationTaskRepository.save(overdueTask);
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

        switch (task.getType()) {
            case DUE_DATE_REMINDER -> handleDueDateReminder(task, loan);
            case OVERDUE_ALERT -> handleOverdueAlert(task, loan);
            default -> throw new IllegalStateException("Unsupported notification type: " + task.getType());
        }
    }

    private void handleDueDateReminder(NotificationTask task, Loan loan) {
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Due date reminder can only be sent for active loans");
        }

        log.info(
                "Due date reminder sent for loanId={}, userId={}, dueDate={}",
                task.getLoanId(),
                task.getUserId(),
                loan.getDueDate()
        );
    }

    private void handleOverdueAlert(NotificationTask task, Loan loan) {
        if (loan.getStatus() != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Overdue alert can only be sent for overdue loans");
        }

        log.info(
                "Overdue alert sent for loanId={}, userId={}, dueDate={}",
                task.getLoanId(),
                task.getUserId(),
                loan.getDueDate()
        );
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