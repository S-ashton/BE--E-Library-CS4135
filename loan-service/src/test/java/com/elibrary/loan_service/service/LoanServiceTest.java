package com.elibrary.loan_service.service;

import com.elibrary.loan_service.client.BookCopyResponseDTO;
import com.elibrary.loan_service.client.BookServiceClient;
import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.domain.LoanStatus;
import com.elibrary.loan_service.domain.NotificationStatus;
import com.elibrary.loan_service.domain.NotificationTask;
import com.elibrary.loan_service.domain.NotificationType;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.exception.DuplicateActiveLoanException;
import com.elibrary.loan_service.exception.LoanAlreadyReturnedException;
import com.elibrary.loan_service.exception.LoanNotFoundException;
import com.elibrary.loan_service.mapper.LoanMapper;
import com.elibrary.loan_service.messaging.LoanEventPublisher;
import com.elibrary.loan_service.repository.LoanRepository;
import com.elibrary.loan_service.repository.NotificationTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private BookServiceClient bookServiceClient;

    @Mock
    private LoanEventPublisher loanEventPublisher;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private LoanService loanService;

    private BorrowRequestDTO borrowRequest;
    private Loan activeLoan;
    private Loan returnedLoan;
    private UUID loanId;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();

        borrowRequest = new BorrowRequestDTO(1L, "user@example.com");

        activeLoan = new Loan(
                1L,
                1L,
                2L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(13),
                LoanStatus.ACTIVE
        );
        setLoanId(activeLoan, loanId);

        returnedLoan = new Loan(
                1L,
                1L,
                2L,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1),
                LoanStatus.RETURNED
        );
        setLoanId(returnedLoan, UUID.randomUUID());

        lenient().when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("borrowBook creates a loan successfully")
    void borrowBook_success() {
        LoanDTO mappedDto = mock(LoanDTO.class);

        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.ACTIVE))
                .thenReturn(false);
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.OVERDUE))
                .thenReturn(false);

        when(bookServiceClient.getAvailableCopy(1L, "Bearer token"))
                .thenReturn(new BookCopyResponseDTO(2L, 1L, "AVAILABLE"));

        when(loanMapper.toDto(any(Loan.class))).thenReturn(mappedDto);

        LoanDTO result = loanService.borrowBook(1L, borrowRequest, "Bearer token");

        assertNotNull(result);

        verify(bookServiceClient).getAvailableCopy(1L, "Bearer token");
        verify(bookServiceClient).changeCopyStatus(2L, "ON_LOAN", 1L, "Bearer token");
        verify(emailNotificationService).sendBorrowConfirmation(eq("user@example.com"), any(Loan.class));
        verify(notificationTaskRepository).save(any(NotificationTask.class));
        verify(loanEventPublisher).publishLoanBorrowed(any());

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(loanCaptor.capture());

        Loan savedLoan = loanCaptor.getValue();
        assertEquals(1L, savedLoan.getUserId());
        assertEquals(1L, savedLoan.getBookId());
        assertEquals(2L, savedLoan.getCopyId());
        assertEquals(LoanStatus.ACTIVE, savedLoan.getStatus());
        assertNotNull(savedLoan.getBorrowDate());
        assertNotNull(savedLoan.getDueDate());
        assertEquals(BigDecimal.ZERO, savedLoan.getFineAmount());

        ArgumentCaptor<NotificationTask> taskCaptor = ArgumentCaptor.forClass(NotificationTask.class);
        verify(notificationTaskRepository).save(taskCaptor.capture());

        NotificationTask task = taskCaptor.getValue();
        assertNotNull(task);
    }

    @Test
    @DisplayName("borrowBook throws when duplicate active loan exists")
    void borrowBook_duplicateActiveLoan() {
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.ACTIVE))
                .thenReturn(true);
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.OVERDUE))
                .thenReturn(false);

        assertThrows(
                DuplicateActiveLoanException.class,
                () -> loanService.borrowBook(1L, borrowRequest, "Bearer token")
        );

        verify(bookServiceClient, never()).getAvailableCopy(anyLong(), anyString());
        verify(bookServiceClient, never()).changeCopyStatus(anyLong(), anyString(), anyLong(), anyString());
        verify(loanRepository, never()).save(any());
        verify(notificationTaskRepository, never()).save(any());
        verify(emailNotificationService, never()).sendBorrowConfirmation(anyString(), any(Loan.class));
    }

    @Test
    @DisplayName("borrowBook throws when duplicate overdue loan exists")
    void borrowBook_duplicateOverdueLoan() {
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.ACTIVE))
                .thenReturn(false);
        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, 1L, LoanStatus.OVERDUE))
                .thenReturn(true);

        assertThrows(
                DuplicateActiveLoanException.class,
                () -> loanService.borrowBook(1L, borrowRequest, "Bearer token")
        );

        verify(bookServiceClient, never()).getAvailableCopy(anyLong(), anyString());
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("returnLoan returns an active loan successfully")
    void returnLoan_success() {
        LoanDTO mappedDto = mock(LoanDTO.class);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(activeLoan));
        when(notificationTaskRepository.findByLoanIdAndStatus(loanId, NotificationStatus.PENDING))
                .thenReturn(List.of());
        when(loanMapper.toDto(any(Loan.class))).thenReturn(mappedDto);

        LoanDTO result = loanService.returnLoan(loanId, "Bearer token");

        assertNotNull(result);

        verify(bookServiceClient).changeCopyStatus(
                activeLoan.getCopyId(),
                "AVAILABLE",
                activeLoan.getUserId(),
                "Bearer token"
        );
        verify(loanEventPublisher).publishLoanReturned(any());

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(loanCaptor.capture());

        Loan savedLoan = loanCaptor.getValue();
        assertEquals(LoanStatus.RETURNED, savedLoan.getStatus());
        assertNotNull(savedLoan.getReturnDate());
        assertNotNull(savedLoan.getFineAmount());
    }

    @Test
    @DisplayName("returnLoan throws when loan already returned")
    void returnLoan_alreadyReturned() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(returnedLoan));

        assertThrows(
                LoanAlreadyReturnedException.class,
                () -> loanService.returnLoan(loanId, "Bearer token")
        );

        verify(bookServiceClient, never()).changeCopyStatus(anyLong(), anyString(), anyLong(), anyString());
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("returnLoan throws when loan not found")
    void returnLoan_notFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(
                LoanNotFoundException.class,
                () -> loanService.returnLoan(loanId, "Bearer token")
        );

        verify(bookServiceClient, never()).changeCopyStatus(anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("getLoanHistory returns mapped DTO list")
    void getLoanHistory_success() {
        LoanDTO dto = mock(LoanDTO.class);

        when(loanRepository.findByUserIdOrderByBorrowDateDesc(1L))
                .thenReturn(List.of(activeLoan));

        when(loanMapper.toDto(activeLoan)).thenReturn(dto);

        List<LoanDTO> result = loanService.getLoanHistory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(loanRepository).findByUserIdOrderByBorrowDateDesc(1L);
        verify(loanMapper).toDto(activeLoan);
    }

    @Test
    @DisplayName("processOverdueLoans marks overdue loan and creates overdue notification")
    void processOverdueLoans_success() {
        Loan overdueLoan = new Loan(
                1L,
                1L,
                2L,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(2),
                LoanStatus.ACTIVE
        );
        UUID overdueLoanId = UUID.randomUUID();
        setLoanId(overdueLoan, overdueLoanId);

        NotificationTask existingReminder = mock(NotificationTask.class);
        when(existingReminder.getRecipientEmail()).thenReturn("user@example.com");

        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueLoan));

        when(notificationTaskRepository.existsByLoanIdAndTypeAndScheduledAtBetween(
                eq(overdueLoanId),
                eq(NotificationType.OVERDUE_ALERT),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(false);

        when(notificationTaskRepository.findTopByLoanIdOrderByScheduledAtAsc(overdueLoanId))
                .thenReturn(Optional.of(existingReminder));

        loanService.processOverdueLoans();

        verify(loanRepository).save(overdueLoan);
        verify(notificationTaskRepository).save(any(NotificationTask.class));
        assertEquals(LoanStatus.OVERDUE, overdueLoan.getStatus());
    }

    @Test
    @DisplayName("processOverdueLoans does not create duplicate overdue notification for same day")
    void processOverdueLoans_skipsDuplicateOverdueNotification() {
        Loan overdueLoan = new Loan(
                1L,
                1L,
                2L,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(2),
                LoanStatus.ACTIVE
        );
        UUID overdueLoanId = UUID.randomUUID();
        setLoanId(overdueLoan, overdueLoanId);

        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueLoan));

        when(notificationTaskRepository.existsByLoanIdAndTypeAndScheduledAtBetween(
                eq(overdueLoanId),
                eq(NotificationType.OVERDUE_ALERT),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(true);

        loanService.processOverdueLoans();

        verify(loanRepository).save(overdueLoan);
        verify(notificationTaskRepository, never()).save(any(NotificationTask.class));
        assertEquals(LoanStatus.OVERDUE, overdueLoan.getStatus());
    }

    @Test
    @DisplayName("processNotificationTasks sends reminder successfully")
    void processNotificationTasks_success() {
        NotificationTask task = mock(NotificationTask.class);
        UUID notificationLoanId = UUID.randomUUID();

        when(task.getLoanId()).thenReturn(notificationLoanId);
        when(task.getType()).thenReturn(NotificationType.DUE_DATE_REMINDER);

        when(notificationTaskRepository.findByStatusAndScheduledAtBefore(
                eq(NotificationStatus.PENDING),
                any(LocalDateTime.class)
        )).thenReturn(List.of(task));

        when(loanRepository.findById(notificationLoanId)).thenReturn(Optional.of(activeLoan));

        loanService.processNotificationTasks();

        verify(emailNotificationService).sendNotification(task, activeLoan);
        verify(task).markSent();
        verify(notificationTaskRepository).save(task);
    }

    @Test
    @DisplayName("processNotificationTasks marks task failed when sending fails")
    void processNotificationTasks_failure() {
        NotificationTask task = mock(NotificationTask.class);
        UUID notificationLoanId = UUID.randomUUID();

        when(task.getLoanId()).thenReturn(notificationLoanId);
        when(task.getRetryCount()).thenReturn(1);

        when(notificationTaskRepository.findByStatusAndScheduledAtBefore(
                eq(NotificationStatus.PENDING),
                any(LocalDateTime.class)
        )).thenReturn(List.of(task));

        when(loanRepository.findById(notificationLoanId)).thenReturn(Optional.of(activeLoan));

        doThrow(new RuntimeException("email failed"))
                .when(emailNotificationService)
                .sendNotification(task, activeLoan);

        loanService.processNotificationTasks();

        verify(task).markFailed();
        verify(notificationTaskRepository).save(task);
    }

    @Test
    @DisplayName("processNotificationTasks marks task dead after max retries")
    void processNotificationTasks_marksDeadAfterMaxRetries() {
        NotificationTask task = mock(NotificationTask.class);
        UUID notificationLoanId = UUID.randomUUID();

        when(task.getLoanId()).thenReturn(notificationLoanId);
        when(task.getRetryCount()).thenReturn(3);

        when(notificationTaskRepository.findByStatusAndScheduledAtBefore(
                eq(NotificationStatus.PENDING),
                any(LocalDateTime.class)
        )).thenReturn(List.of(task));

        when(loanRepository.findById(notificationLoanId)).thenReturn(Optional.of(activeLoan));

        doThrow(new RuntimeException("email failed"))
                .when(emailNotificationService)
                .sendNotification(task, activeLoan);

        loanService.processNotificationTasks();

        verify(task).markFailed();
        verify(task).markDead();
        verify(notificationTaskRepository).save(task);
    }

    private void setLoanId(Loan loan, UUID id) {
        try {
            Field field = Loan.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(loan, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set loan id in test", e);
        }
    }
}