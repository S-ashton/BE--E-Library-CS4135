package com.elibrary.user_service.messaging;

import java.time.Instant;

public class UserEmailUpdatedEvent {

    private Long userId;
    private String newEmail;
    private Instant timestamp;

    public UserEmailUpdatedEvent() {
    }

    public UserEmailUpdatedEvent(Long userId, String newEmail, Instant timestamp) {
        this.userId = userId;
        this.newEmail = newEmail;
        this.timestamp = timestamp;
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
