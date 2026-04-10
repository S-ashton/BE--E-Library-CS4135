package com.elibrary.book_service.exceptions;

public class TitleNotFoundException extends RuntimeException{
    public TitleNotFoundException(String message){
        super(message);
    }
}
