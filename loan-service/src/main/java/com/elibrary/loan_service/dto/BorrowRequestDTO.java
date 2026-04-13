package com.elibrary.loan_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Borrow request payload")
public class BorrowRequestDTO {

    @Schema(description = "Book title ID to borrow", example = "1")
    @NotNull(message = "Book ID is required")
    private Long bookId;

    @Schema(description = "Recipient email for reminders and overdue alerts", example = "alice@elibrary.ie")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    public BorrowRequestDTO() {
    }

    public BorrowRequestDTO(Long bookId, String email) {
        this.bookId = bookId;
        this.email = email;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}