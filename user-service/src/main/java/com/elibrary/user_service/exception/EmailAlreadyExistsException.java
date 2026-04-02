package com.elibrary.user_service.exception;

// Thrown when a registration attempt uses an email address already in use
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}
