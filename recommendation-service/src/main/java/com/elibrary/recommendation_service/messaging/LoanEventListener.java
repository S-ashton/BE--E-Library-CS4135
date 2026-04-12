package com.elibrary.recommendation_service.messaging;

import com.elibrary.recommendation_service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoanEventListener {

    private static final Logger log = LoggerFactory.getLogger(LoanEventListener.class);

    private final FileStorageService storage;

    public LoanEventListener(FileStorageService storage) {
        this.storage = storage;
    }

    @RabbitListener(queues = "${loan.events.queue}")
    public void onLoanBorrowed(@Payload Map<String, Object> event) {
        String userId = String.valueOf(event.get("userId"));
        String bookId = String.valueOf(event.get("bookId"));

        log.info("Received loan.borrowed event: userId={}, bookId={}", userId, bookId);

        Map<String, List<String>> loans = storage.load("data/loans.json", Map.class);
        if (loans == null) loans = new HashMap<>();

        loans.computeIfAbsent(userId, k -> new ArrayList<>());
        if (!loans.get(userId).contains(bookId)) {
            loans.get(userId).add(bookId);
            storage.save("data/loans.json", loans);
        }
    }
}
