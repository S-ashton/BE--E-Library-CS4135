package com.elibrary.recommendation_service.messaging;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.service.BookUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BookEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookEventListener.class);

    private final BookUpdateService bookUpdateService;

    public BookEventListener(BookUpdateService bookUpdateService) {
        this.bookUpdateService = bookUpdateService;
    }

    @RabbitListener(queues = "${book.events.queue}")
    public void onBookAdded(@Payload Map<String, Object> event) {
        Book book = new Book(
                asLong(event.get("id")),
                asString(event.get("title")),
                asString(event.get("description"))
        );

        log.info("Received book.added event: bookId={}", book.getId());
        bookUpdateService.updateBook(book);
    }

    @RabbitListener(queues = "${book.events.deleted-queue}")
    public void onBookDeleted(@Payload Map<String, Object> event) {
        Long bookId = asLong(event.get("id"));
        log.info("Received book.deleted event: bookId={}", bookId);
        bookUpdateService.removeBook(bookId);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            throw new IllegalArgumentException("book.added event is missing id");
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
