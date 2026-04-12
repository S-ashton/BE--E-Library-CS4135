package com.elibrary.book_service.exceptions;

public class TitleAlreadyExistsException extends RuntimeException {
    private final Long existingId;

    public TitleAlreadyExistsException(String message){
        super(message);
        this.existingId = null;
    }

    public TitleAlreadyExistsException(String message, Long existingId){
        super(message);
        this.existingId = existingId;
    }

    public Long getExistingId(){
        return existingId;
    }
}
