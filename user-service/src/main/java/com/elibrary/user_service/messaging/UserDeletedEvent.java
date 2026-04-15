package com.elibrary.user_service.messaging;

import java.time.Instant;

public class UserDeletedEvent {

    private Long userId;
    private String email;
    private Instant timestamp;

    public UserDeletedEvent() {
    }

    public UserDeletedEvent(Long userId, String email, Instant timestamp) {
        this.userId = userId;
        this.email = email;
        this.timestamp = timestamp;
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
