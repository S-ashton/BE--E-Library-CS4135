package com.elibrary.loan_service.client;

public class BookCopyResponseDTO {

    private Long id;
    private Long bookId;
    private String status;

    public BookCopyResponseDTO() {
    }

    public BookCopyResponseDTO(Long id, Long bookId, String status) {
        this.id = id;
        this.bookId = bookId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getStatus() {
        return status;
    }
}