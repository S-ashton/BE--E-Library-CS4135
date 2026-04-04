package com.elibrary.loan_service.exception;

public class DuplicateActiveLoanException extends RuntimeException {
    public DuplicateActiveLoanException(String message) {
        super(message);
    }
}