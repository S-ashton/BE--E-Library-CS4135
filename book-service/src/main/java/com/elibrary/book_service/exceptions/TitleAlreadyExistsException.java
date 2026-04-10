package com.elibrary.book_service.exceptions;

public class TitleAlreadyExistsException extends RuntimeException {
    public TitleAlreadyExistsException(String message){
        super(message);
    }
}
