package com.elibrary.recommendation_service.service;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BookUpdateServiceTest {

    private FileStorageService storage;
    private BookEmbeddingCache embeddingCache;
    private BookUpdateService service;

    @BeforeEach
    void setup() {
        storage = mock(FileStorageService.class);
        embeddingCache = mock(BookEmbeddingCache.class);
        service = new BookUpdateService(storage, embeddingCache);
    }

    @Test
    void updatesStoredBookAndEmbedding() {
        List<Map<String, Object>> existingBooks = new ArrayList<>();
        existingBooks.add(Map.of(
                "id", 1,
                "title", "Old title",
                "description", "Old description"
        ));

        when(storage.load(eq("data/books.json"), eq(List.class)))
                .thenReturn(existingBooks);

        Book book = new Book(1L, "New title", "New description");

        service.updateBook(book);

        ArgumentCaptor<List> savedBooks = ArgumentCaptor.forClass(List.class);
        verify(storage).save(eq("data/books.json"), savedBooks.capture());
        verify(embeddingCache).addOrUpdateBook(book);

        List<Map<String, Object>> saved = savedBooks.getValue();
        assertEquals(1, saved.size());
        assertEquals(1L, ((Number) saved.getFirst().get("id")).longValue());
        assertEquals("New title", saved.getFirst().get("title"));
        assertEquals("New description", saved.getFirst().get("description"));
    }
}
