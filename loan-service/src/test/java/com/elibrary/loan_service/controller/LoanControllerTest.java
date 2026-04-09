package com.elibrary.loan_service.controller;

import com.elibrary.loan_service.domain.LoanStatus;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.service.LoanService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;


    @Test
    @DisplayName("POST /api/loans returns 201 and loan dto")
    void borrowBook_returnsCreatedLoan() throws Exception {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        LoanDTO response = new LoanDTO(
                loanId,
                1L,
                bookId,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(14),
                null,
                LoanStatus.ACTIVE,
                BigDecimal.ZERO
        );

        when(loanService.borrowBook(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/loans")
                        .header("X-Authenticated-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "%s"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(loanId.toString()))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.fineAmount").value(0));
    }

    @Test
    @DisplayName("POST /api/loans with invalid body returns 400")
    void borrowBook_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/loans")
                        .header("X-Authenticated-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/loans/{id}/return returns 200 and updated loan")
    void returnLoan_returnsUpdatedLoan() throws Exception {
        UUID loanId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        LoanDTO response = new LoanDTO(
                loanId,
                1L,
                bookId,
                LocalDateTime.now().minusDays(15),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                LoanStatus.RETURNED,
                BigDecimal.valueOf(2)
        );

        when(loanService.returnLoan(loanId)).thenReturn(response);

        mockMvc.perform(post("/api/loans/{id}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId.toString()))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.fineAmount").value(2));
    }

    @Test
    @DisplayName("GET /api/loans/history returns loan list")
    void getLoanHistory_returnsLoanList() throws Exception {
        UUID loan1 = UUID.randomUUID();
        UUID loan2 = UUID.randomUUID();
        UUID book1 = UUID.randomUUID();
        UUID book2 = UUID.randomUUID();

        LoanDTO first = new LoanDTO(
                loan1, 1L, book1,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(14),
                null,
                LoanStatus.ACTIVE,
                BigDecimal.ZERO
        );

        LoanDTO second = new LoanDTO(
                loan2, 1L, book2,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(6),
                LocalDateTime.now().minusDays(5),
                LoanStatus.RETURNED,
                BigDecimal.ZERO
        );

        when(loanService.getLoanHistory(1L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/loans/history")
                        .header("X-Authenticated-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(loan1.toString()))
                .andExpect(jsonPath("$[1].id").value(loan2.toString()));
    }
}