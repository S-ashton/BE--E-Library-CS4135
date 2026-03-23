package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UpdateControllerTest {

    private FileStorageService storage;
    private BookEmbeddingCache embeddingCache;
    private UpdateController controller;

    @BeforeEach
    void setup() {
        storage = mock(FileStorageService.class);
        embeddingCache = mock(BookEmbeddingCache.class);
        controller = new UpdateController(storage, embeddingCache);
    }

    @Test
    void updatesBookAndEmbedsIt() {
        Book book = new Book(1L, "Title", "Desc");

        when(storage.load(eq("books.json"), eq(List.class)))
                .thenReturn(new ArrayList<>());

        controller.updateBook(book);

        verify(storage).save(eq("books.json"), any());
        verify(embeddingCache).addOrUpdateBook(book);
    }

    @Test
    void updatesLoanList() {
        Map<String, List<String>> loans = new HashMap<>();
        loans.put("user1", new ArrayList<>(List.of("1")));

        when(storage.load(eq("loans.json"), eq(Map.class)))
                .thenReturn(loans);

        controller.updateLoan(Map.of("userId", "user1", "bookId", "2"));

        verify(storage).save(eq("loans.json"), any());
        assertTrue(loans.get("user1").contains("2"));
    }
}


