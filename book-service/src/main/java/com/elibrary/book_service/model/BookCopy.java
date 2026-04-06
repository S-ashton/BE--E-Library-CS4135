package com.elibrary.book_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name=book_id)
    private Long book_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected BookCopy() {}

    public BookCopy(Long book_id, Status status) {
        this(book_id, status);
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return book_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
