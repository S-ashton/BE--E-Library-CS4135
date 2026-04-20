package com.elibrary.loan_service.controller;

import com.elibrary.loan_service.domain.LoanStatus;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.exception.DuplicateActiveLoanException;
import com.elibrary.loan_service.exception.LoanAlreadyReturnedException;
import com.elibrary.loan_service.exception.LoanNotFoundException;
import com.elibrary.loan_service.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    private static final String AUTH_USER_HEADER = "X-Authenticated-User-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String GATEWAY_USER_HEADER = "X-Authenticated-User";
    private static final String GATEWAY_ROLE_HEADER = "X-Authenticated-Role";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    @Test
    @DisplayName("POST /api/loans creates a loan")
    void borrowBook_success() throws Exception {
        LoanDTO response = buildLoanDto(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                1L,
                1L,
                2L,
                LoanStatus.ACTIVE,
                null,
                BigDecimal.ZERO
        );

        BorrowRequestDTO request = new BorrowRequestDTO(1L, "22367543@studentmail.ul.ie");

        when(loanService.borrowBook(
                eq(1L),
                any(BorrowRequestDTO.class),
                eq("Bearer test-token")
        )).thenReturn(response);

        mockMvc.perform(post("/api/loans")
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTH_USER_HEADER, "1")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.copyId").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.fineAmount").value(0));

        verify(loanService).borrowBook(
                eq(1L),
                any(BorrowRequestDTO.class),
                eq("Bearer test-token")
        );
    }

    @Test
    @DisplayName("POST /api/loans returns 409 when user already has an active loan for the same book")
    void borrowBook_duplicateLoan_returns409() throws Exception {
        BorrowRequestDTO request = new BorrowRequestDTO(1L, "22367543@studentmail.ul.ie");

        when(loanService.borrowBook(
                eq(1L),
                any(BorrowRequestDTO.class),
                eq("Bearer test-token")
        )).thenThrow(new DuplicateActiveLoanException("User already has an active loan for this book"));

        mockMvc.perform(post("/api/loans")
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTH_USER_HEADER, "1")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(loanService).borrowBook(
                eq(1L),
                any(BorrowRequestDTO.class),
                eq("Bearer test-token")
        );
    }

    @Test
    @DisplayName("POST /api/loans returns 401 when authenticated user header is missing")
    void borrowBook_missingUserHeader_returns401() throws Exception {
        BorrowRequestDTO request = new BorrowRequestDTO(1L, "22367543@studentmail.ul.ie");

        mockMvc.perform(post("/api/loans")
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Missing required header: X-Authenticated-User-Id"));
    }

    @Test
    @DisplayName("GET /api/loans/history returns loan history for authenticated user")
    void getLoanHistory_success() throws Exception {
        LoanDTO activeLoan = buildLoanDto(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                1L,
                1L,
                2L,
                LoanStatus.ACTIVE,
                null,
                BigDecimal.ZERO
        );

        LoanDTO returnedLoan = buildLoanDto(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                1L,
                1L,
                1L,
                LoanStatus.RETURNED,
                LocalDateTime.now(),
                BigDecimal.ZERO
        );

        when(loanService.getLoanHistory(1L)).thenReturn(List.of(activeLoan, returnedLoan));

        mockMvc.perform(get("/api/loans/history")
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTH_USER_HEADER, "1")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .andExpect(jsonPath("$[1].status").value("RETURNED"));

        verify(loanService).getLoanHistory(1L);
    }

    @Test
    @DisplayName("GET /api/loans/history returns 401 when authenticated user header is missing")
    void getLoanHistory_missingUserHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/loans/history")
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Missing required header: X-Authenticated-User-Id"));
    }

    @Test
    @DisplayName("POST /api/loans/{loanId}/return returns updated returned loan")
    void returnLoan_success() throws Exception {
        UUID loanId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        LoanDTO returnedLoan = buildLoanDto(
                loanId,
                1L,
                1L,
                2L,
                LoanStatus.RETURNED,
                LocalDateTime.now(),
                BigDecimal.ZERO
        );

        when(loanService.returnLoan(eq(loanId), eq("Bearer test-token")))
                .thenReturn(returnedLoan);

        mockMvc.perform(post("/api/loans/{loanId}/return", loanId)
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());

        verify(loanService).returnLoan(eq(loanId), eq("Bearer test-token"));
    }

    @Test
    @DisplayName("POST /api/loans/{loanId}/return returns 409 when loan already returned")
    void returnLoan_alreadyReturned_returns409() throws Exception {
        UUID loanId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        when(loanService.returnLoan(eq(loanId), eq("Bearer test-token")))
                .thenThrow(new LoanAlreadyReturnedException("Loan is already returned"));

        mockMvc.perform(post("/api/loans/{loanId}/return", loanId)
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/loans/{loanId}/return returns 404 when loan does not exist")
    void returnLoan_notFound_returns404() throws Exception {
        UUID loanId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

        when(loanService.returnLoan(eq(loanId), eq("Bearer test-token")))
                .thenThrow(new LoanNotFoundException("Loan not found"));

        mockMvc.perform(post("/api/loans/{loanId}/return", loanId)
                        .header(GATEWAY_USER_HEADER, "test-user@elibrary.ie")
                        .header(GATEWAY_ROLE_HEADER, "USER")
                        .header(AUTHORIZATION_HEADER, "Bearer test-token"))
                .andExpect(status().isNotFound());
    }

    private LoanDTO buildLoanDto(
            UUID id,
            Long userId,
            Long bookId,
            Long copyId,
            LoanStatus status,
            LocalDateTime returnDate,
            BigDecimal fineAmount
    ) {
        return new LoanDTO(
                id,
                userId,
                bookId,
                copyId,
                LocalDateTime.of(2026, 4, 12, 1, 7, 52),
                LocalDateTime.of(2026, 4, 26, 1, 7, 52),
                returnDate,
                status,
                fineAmount
        );
    }
}