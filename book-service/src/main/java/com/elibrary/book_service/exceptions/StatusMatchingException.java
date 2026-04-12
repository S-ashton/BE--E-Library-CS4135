package com.elibrary.book_service.exceptions;

public class StatusMatchingException extends RuntimeException{
    public StatusMatchingException(String message){
        super(message);
    }
}
