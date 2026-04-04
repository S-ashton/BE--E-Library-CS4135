package com.elibrary.loan_service.exception;

public class UnauthenticatedRequestException extends RuntimeException {
    public UnauthenticatedRequestException(String message) {
        super(message);
    }
}