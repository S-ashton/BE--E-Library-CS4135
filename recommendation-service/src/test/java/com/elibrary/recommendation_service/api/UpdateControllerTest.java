package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UpdateControllerTest {

    private FileStorageService storage;
    private UpdateController controller;

    @BeforeEach
    void setup() throws Exception {
        storage = new FileStorageService();
        controller = new UpdateController(storage);

        // Ensure clean test environment
        Files.deleteIfExists(Path.of("books.json"));
        Files.deleteIfExists(Path.of("loans.json"));
    }

    @Test
    void addsNewBookToFile() throws Exception {
        Book book = new Book(1L, "Book A", "A story about magic");

        controller.updateBook(book);

        // Load raw JSON (LinkedHashMap)
        List<Map<String, Object>> rawBooks =
                storage.load("books.json", List.class);

        assertNotNull(rawBooks);
        assertEquals(1, rawBooks.size());

        Map<String, Object> b = rawBooks.get(0);

        assertEquals(1L, ((Number) b.get("id")).longValue());
        assertEquals("Book A", b.get("title"));
        assertEquals("A story about magic", b.get("description"));
    }

    @Test
    void addsLoanToFile() throws Exception {
        controller.updateLoan(Map.of("userId", "user1", "bookId", 1));

        Map<String, List<String>> loans =
                storage.load("loans.json", Map.class);

        assertNotNull(loans);
        assertEquals(List.of("1"), loans.get("user1"));
    }
}

