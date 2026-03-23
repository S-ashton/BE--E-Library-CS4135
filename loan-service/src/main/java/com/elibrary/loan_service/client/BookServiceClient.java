package com.elibrary.loan_service.client;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookServiceClient {

    public boolean isBookAvailable(UUID bookId) {
        // Temporary stub for ticket 1
        return true;
    }
}