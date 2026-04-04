package com.elibrary.loan_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Borrow request payload")
public class BorrowRequestDTO {

    @Schema(description = "ID of the book to borrow", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "Book ID is required")
    private UUID bookId;

    public BorrowRequestDTO() {
    }

    public BorrowRequestDTO(UUID bookId) {
        this.bookId = bookId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }
}