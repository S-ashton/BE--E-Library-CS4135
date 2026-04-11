package com.elibrary.loan_service.service;

import com.elibrary.loan_service.client.BookServiceClient;
import com.elibrary.loan_service.domain.*;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.exception.DuplicateActiveLoanException;
import com.elibrary.loan_service.exception.LoanNotFoundException;
import com.elibrary.loan_service.mapper.LoanMapper;
import com.elibrary.loan_service.messaging.LoanEventPublisher;
import com.elibrary.loan_service.repository.LoanRepository;
import com.elibrary.loan_service.repository.NotificationTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("borrowBook throws when duplicate active loan exists")
    void borrowBook_duplicateActiveLoan_throwsException() {
        UUID bookId = UUID.randomUUID();
        BorrowRequestDTO request = new BorrowRequestDTO(bookId);

        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, bookId, LoanStatus.ACTIVE))
                .thenReturn(true);

        assertThatThrownBy(() -> loanService.borrowBook(1L, request))
                .isInstanceOf(DuplicateActiveLoanException.class)
                .hasMessage("User already has an active loan for this book");

        verify(bookServiceClient, never()).reserveBook(any(), anyLong());
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("borrowBook creates loan, schedules reminder, reserves book and publishes event")
    void borrowBook_success() {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        BorrowRequestDTO request = new BorrowRequestDTO(bookId);

        Loan savedLoan = new Loan(1L, bookId, LocalDateTime.now(), LocalDateTime.now().plusDays(14), LoanStatus.ACTIVE);
        ReflectionTestUtils.setField(savedLoan, "id", loanId);

        LoanDTO dto = new LoanDTO(
                loanId, 1L, bookId,
                savedLoan.getBorrowDate(),
                savedLoan.getDueDate(),
                null,
                LoanStatus.ACTIVE,
                BigDecimal.ZERO
        );

        when(loanRepository.existsByUserIdAndBookIdAndStatus(1L, bookId, LoanStatus.ACTIVE))
                .thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
        when(loanMapper.toDto(savedLoan)).thenReturn(dto);

        LoanDTO result = loanService.borrowBook(1L, request);

        assertThat(result).isSameAs(dto);

        verify(bookServiceClient).reserveBook(bookId, 1L);
        verify(loanRepository).save(any(Loan.class));
        verify(notificationTaskRepository).save(any(NotificationTask.class));
        verify(loanEventPublisher).publishLoanBorrowed(any());
    }

    @Test
    @DisplayName("returnLoan throws 404 style exception when loan does not exist")
    void returnLoan_notFound() {
        UUID loanId = UUID.randomUUID();

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnLoan(loanId))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessage("Loan not found");
    }

    @Test
    @DisplayName("returnLoan marks loan returned, cancels pending tasks, notifies book service and publishes event")
    void returnLoan_success() {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Loan loan = new Loan(
                1L,
                bookId,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(5),
                LoanStatus.ACTIVE
        );
        ReflectionTestUtils.setField(loan, "id", loanId);

        NotificationTask reminderTask = new NotificationTask(
                loanId,
                1L,
                NotificationType.DUE_DATE_REMINDER,
                LocalDateTime.now().minusDays(6)
        );

        LoanDTO dto = new LoanDTO(
                loanId,
                1L,
                bookId,
                loan.getBorrowDate(),
                loan.getDueDate(),
                LocalDateTime.now(),
                LoanStatus.RETURNED,
                BigDecimal.valueOf(10)
        );

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationTaskRepository.findByLoanIdAndStatus(loanId, NotificationStatus.PENDING))
                .thenReturn(List.of(reminderTask));
        when(loanMapper.toDto(any(Loan.class))).thenReturn(dto);

        LoanDTO result = loanService.returnLoan(loanId);

        assertThat(result).isSameAs(dto);
        assertThat(reminderTask.getStatus()).isEqualTo(NotificationStatus.CANCELLED);

        verify(notificationTaskRepository).saveAll(anyList());
        verify(bookServiceClient).returnBook(bookId, 1L);
        verify(loanEventPublisher).publishLoanReturned(any());
    }

    @Test
    @DisplayName("getLoanHistory maps repository results to dto list")
    void getLoanHistory_returnsMappedDtos() {
        UUID book1 = UUID.randomUUID();
        UUID book2 = UUID.randomUUID();

        Loan loan1 = new Loan(1L, book1, LocalDateTime.now(), LocalDateTime.now().plusDays(14), LoanStatus.ACTIVE);
        Loan loan2 = new Loan(1L, book2, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(6), LoanStatus.RETURNED);

        LoanDTO dto1 = new LoanDTO();
        LoanDTO dto2 = new LoanDTO();

        when(loanRepository.findByUserIdOrderByBorrowDateDesc(1L)).thenReturn(List.of(loan1, loan2));
        when(loanMapper.toDto(loan1)).thenReturn(dto1);
        when(loanMapper.toDto(loan2)).thenReturn(dto2);

        List<LoanDTO> result = loanService.getLoanHistory(1L);

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("processOverdueLoans marks active overdue loans and creates overdue alert task once")
    void processOverdueLoans_marksOverdueAndCreatesTask() {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Loan overdueLoan = new Loan(
                1L,
                bookId,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(1),
                LoanStatus.ACTIVE
        );
        ReflectionTestUtils.setField(overdueLoan, "id", loanId);

        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueLoan));
        when(notificationTaskRepository.existsByLoanIdAndTypeAndStatus(
                loanId,
                NotificationType.OVERDUE_ALERT,
                NotificationStatus.PENDING
        )).thenReturn(false);

        loanService.processOverdueLoans();

        assertThat(overdueLoan.getStatus()).isEqualTo(LoanStatus.OVERDUE);

        verify(loanRepository).save(overdueLoan);
        verify(notificationTaskRepository).save(any(NotificationTask.class));
    }

    @Test
    @DisplayName("processOverdueLoans does not create duplicate overdue alert task")
    void processOverdueLoans_doesNotCreateDuplicateAlert() {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Loan overdueLoan = new Loan(
                1L,
                bookId,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(1),
                LoanStatus.ACTIVE
        );
        ReflectionTestUtils.setField(overdueLoan, "id", loanId);

        when(loanRepository.findByStatusAndDueDateBefore(eq(LoanStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(overdueLoan));
        when(notificationTaskRepository.existsByLoanIdAndTypeAndStatus(
                loanId,
                NotificationType.OVERDUE_ALERT,
                NotificationStatus.PENDING
        )).thenReturn(true);

        loanService.processOverdueLoans();

        verify(notificationTaskRepository, never()).save(any(NotificationTask.class));
    }

    @Test
    @DisplayName("processNotificationTasks sends due reminder and marks task sent")
    void processNotificationTasks_dueReminder_marksSent() {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Loan loan = new Loan(
                1L,
                bookId,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(1),
                LoanStatus.ACTIVE
        );
        ReflectionTestUtils.setField(loan, "id", loanId);

        NotificationTask task = new NotificationTask(
                loanId,
                1L,
                NotificationType.DUE_DATE_REMINDER,
                LocalDateTime.now().minusMinutes(1)
        );

        when(notificationTaskRepository.findByStatusAndScheduledAtBefore(eq(NotificationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(task));
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        loanService.processNotificationTasks();

        assertThat(task.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(task.getSentAt()).isNotNull();

        verify(notificationTaskRepository).save(task);
    }

    @Test
    @DisplayName("processNotificationTasks sends overdue alert and marks task sent")
    void processNotificationTasks_overdueAlert_marksSent() {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Loan loan = new Loan(
                1L,
                bookId,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(2),
                LoanStatus.OVERDUE
        );
        ReflectionTestUtils.setField(loan, "id", loanId);

        NotificationTask task = new NotificationTask(
                loanId,
                1L,
                NotificationType.OVERDUE_ALERT,
                LocalDateTime.now().minusMinutes(1)
        );

        when(notificationTaskRepository.findByStatusAndScheduledAtBefore(eq(NotificationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(task));
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        loanService.processNotificationTasks();

        assertThat(task.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(task.getSentAt()).isNotNull();

        verify(notificationTaskRepository).save(task);
    }

    @Test
    @DisplayName("processNotificationTasks marks failed task and increments retry count when processing fails")
    void processNotificationTasks_marksFailedOnError() {
        UUID loanId = UUID.randomUUID();

        NotificationTask task = new NotificationTask(
                loanId,
                1L,
                NotificationType.DUE_DATE_REMINDER,
                LocalDateTime.now().minusMinutes(1)
        );

        when(notificationTaskRepository.findByStatusAndScheduledAtBefore(eq(NotificationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(task));
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        loanService.processNotificationTasks();

        assertThat(task.getStatus()).isIn(NotificationStatus.FAILED, NotificationStatus.DEAD);
        assertThat(task.getRetryCount()).isGreaterThan(0);

        verify(notificationTaskRepository).save(task);
    }
}