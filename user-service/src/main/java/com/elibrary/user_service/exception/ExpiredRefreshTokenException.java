package com.elibrary.user_service.exception;

public class ExpiredRefreshTokenException extends RuntimeException {

    public ExpiredRefreshTokenException(String message) {
        super(message);
    }
}
