package com.elibrary.loan_service.controller;

import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.service.LoanService;
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

    @PostMapping
    public ResponseEntity<LoanDTO> borrowBook(@Valid @RequestBody BorrowRequestDTO request) {
        // Temporary hardcoded userId until JWT extraction is added
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        LoanDTO response = loanService.borrowBook(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}