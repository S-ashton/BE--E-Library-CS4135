package com.elibrary.recommendation_service.messaging;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.service.BookUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BookEventListenerTest {

    private BookUpdateService bookUpdateService;
    private BookEventListener listener;

    @BeforeEach
    void setup() {
        bookUpdateService = mock(BookUpdateService.class);
        listener = new BookEventListener(bookUpdateService);
    }

    @Test
    void consumesBookAddedEvent() {
        listener.onBookAdded(Map.of(
                "id", "42",
                "title", "Title",
                "description", "Description",
                "author", "Author"
        ));

        ArgumentCaptor<Book> book = ArgumentCaptor.forClass(Book.class);
        verify(bookUpdateService).updateBook(book.capture());

        assertEquals(42L, book.getValue().getId());
        assertEquals("Title", book.getValue().getTitle());
        assertEquals("Description", book.getValue().getDescription());
    }
}
