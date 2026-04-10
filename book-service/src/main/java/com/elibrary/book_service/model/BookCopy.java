package com.elibrary.book_service.model;

import com.elibrary.book_service.dto.CopyResponseDTO;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected BookCopy() {}

    public BookCopy(Long bookId, Status status) {
        this.bookId = bookId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public CopyResponseDTO toDto(){
        return new CopyResponseDTO(this.id, this.bookId, this.status);
    }
}
