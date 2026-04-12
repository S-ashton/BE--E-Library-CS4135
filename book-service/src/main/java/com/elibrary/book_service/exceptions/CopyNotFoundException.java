package com.elibrary.book_service.exceptions;

public class CopyNotFoundException extends RuntimeException {
    public CopyNotFoundException(String message){
        super(message);
    }
}
