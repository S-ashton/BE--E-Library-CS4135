package com.elibrary.loan_service.controller;

import com.elibrary.loan_service.dto.ActiveLoanDTO;
import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.elibrary.loan_service.exception.UnauthenticatedRequestException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "Endpoints for borrowing, returning, and viewing loan history")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Operation(
            summary = "Borrow a book",
            description = "Creates a new loan for the authenticated user if the book is available and there is no duplicate active loan."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan created successfully",
                    content = @Content(schema = @Schema(implementation = LoanDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Book unavailable or duplicate active loan", content = @Content)
    })
    @PostMapping
    public ResponseEntity<LoanDTO> borrowBook(
            @Valid @RequestBody BorrowRequestDTO request,
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        LoanDTO response = loanService.borrowBook(userId, request, authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Return a book",
            description = "Marks the specified loan as returned, calculates any overdue fine, and cancels pending reminder tasks."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan returned successfully",
                    content = @Content(schema = @Schema(implementation = LoanDTO.class))),
            @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Loan already returned", content = @Content)
    })
    @PostMapping("/{id}/return")
    public ResponseEntity<LoanDTO> returnLoan(
            @PathVariable("id") UUID loanId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        LoanDTO response = loanService.returnLoan(loanId, authorization);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get loan history for the authenticated user",
            description = "Returns all loans for the authenticated user, ordered by most recent borrow date first."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan history returned successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content)
    })
    @GetMapping("/history")
    public ResponseEntity<List<LoanDTO>> getLoanHistory(
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId
    ) {
        List<LoanDTO> response = loanService.getLoanHistory(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all active loans",
            description = "Returns all currently active loans across all users. Restricted to STAFF and ADMIN roles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active loans returned successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content)
    })
    @GetMapping("/active")
    public ResponseEntity<List<ActiveLoanDTO>> getAllActiveLoans(
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-Role") String role
    ) {
        if (!"STAFF".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new UnauthenticatedRequestException("Insufficient permissions");
        }
        List<ActiveLoanDTO> response = loanService.getAllActiveLoans();
        return ResponseEntity.ok(response);
    }
}