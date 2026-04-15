package com.elibrary.book_service.messaging;

public class BookDeletedEvent {

    private Long id;

    public BookDeletedEvent() {}

    public BookDeletedEvent(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
