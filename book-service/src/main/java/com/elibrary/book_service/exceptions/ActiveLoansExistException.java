package com.elibrary.book_service.exceptions;

public class ActiveLoansExistException extends RuntimeException {
    public ActiveLoansExistException(String message) {
        super(message);
    }
}
