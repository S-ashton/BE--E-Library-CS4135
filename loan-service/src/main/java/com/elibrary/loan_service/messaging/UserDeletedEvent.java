package com.elibrary.loan_service.messaging;

import java.time.Instant;

public class UserDeletedEvent {

    private Long userId;
    private String email;
    private Instant timestamp;

    public UserDeletedEvent() {
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
