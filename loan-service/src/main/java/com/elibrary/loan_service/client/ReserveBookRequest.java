package com.elibrary.loan_service.client;

import java.util.UUID;

public class ReserveBookRequest {

    private UUID userId;

    public ReserveBookRequest() {
    }

    public ReserveBookRequest(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}