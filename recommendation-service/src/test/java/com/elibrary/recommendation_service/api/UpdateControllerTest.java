package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.LoanRecord;
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

        when(storage.load(eq("data/books.json"), eq(List.class)))
                .thenReturn(new ArrayList<>());

        controller.updateBook(book);

        verify(storage).save(eq("data/books.json"), any());
        verify(embeddingCache).addOrUpdateBook(book);
    }

    @Test
    void updatesLoanList() {
        Map<String, List<String>> loans = new HashMap<>();
        loans.put("user1", new ArrayList<>(List.of("1")));

        when(storage.load(eq("data/loans.json"), eq(Map.class)))
                .thenReturn(loans);

        // FIX: Use LoanRecord instead of Map
        LoanRecord record = new LoanRecord(
                100L,
                1L,
                2L,
                "2025-02-14T10:23:54Z"
        );

        controller.updateLoan(record);

        verify(storage).save(eq("data/loans.json"), any());
        assertTrue(loans.get("1").contains("2"));
    }
}



