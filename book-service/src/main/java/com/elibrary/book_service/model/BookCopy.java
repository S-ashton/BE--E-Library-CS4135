package com.elibrary.book_service.model;

import com.elibrary.book_service.dto.CopyResponseDTO;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="book_id")
    private Long book_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected BookCopy() {}

    public BookCopy(Long book_id, Status status) {
        this.book_id = book_id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return book_id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public CopyResponseDTO toDto(){
        return new CopyResponseDTO(this.id, this.book_id, this.status);
    }
}
