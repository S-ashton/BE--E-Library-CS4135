package com.elibrary.loan_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BorrowRequestDTO {

    @NotNull
    private UUID bookId;

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }
}