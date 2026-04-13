package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.LoanRecord;
import com.elibrary.recommendation_service.service.BookUpdateService;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UpdateControllerTest {

    private FileStorageService storage;
    private BookUpdateService bookUpdateService;
    private UpdateController controller;

    @BeforeEach
    void setup() {
        storage = mock(FileStorageService.class);
        bookUpdateService = mock(BookUpdateService.class);
        controller = new UpdateController(storage, bookUpdateService);
    }

    @Test
    void delegatesBookUpdate() {
        Book book = new Book(1L, "Title", "Desc");

        controller.updateBook(book);

        verify(bookUpdateService).updateBook(book);
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



