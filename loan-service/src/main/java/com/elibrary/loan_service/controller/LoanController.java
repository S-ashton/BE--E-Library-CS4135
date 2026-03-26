package com.elibrary.loan_service.controller;

import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Operation(summary = "Borrow a book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity"),
            @ApiResponse(responseCode = "409", description = "Book unavailable or duplicate active loan")
    })
    @PostMapping
    public ResponseEntity<LoanDTO> borrowBook(
            @Valid @RequestBody BorrowRequestDTO request,
            @Parameter(description = "Temporary user identity header for testing")
            @RequestHeader("X-User-Id") UUID userId
    ) {
        LoanDTO response = loanService.borrowBook(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}