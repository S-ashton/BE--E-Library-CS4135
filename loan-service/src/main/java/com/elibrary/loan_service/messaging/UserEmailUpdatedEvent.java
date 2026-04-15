package com.elibrary.loan_service.messaging;

import java.time.Instant;

public class UserEmailUpdatedEvent {

    private Long userId;
    private String newEmail;
    private Instant timestamp;

    public UserEmailUpdatedEvent() {
    }

    public Long getUserId() {
        return userId;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
