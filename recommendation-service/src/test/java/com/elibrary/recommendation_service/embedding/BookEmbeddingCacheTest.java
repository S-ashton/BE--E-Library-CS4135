package com.elibrary.recommendation_service.embedding;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BookEmbeddingCacheTest {

    private FileStorageService storage;
    private EmbeddingClient embeddingClient;
    private BookEmbeddingCache cache;

    @BeforeEach
    void setup() {
        storage = mock(FileStorageService.class);
        embeddingClient = mock(EmbeddingClient.class);

        // Mock existing embeddings
        when(storage.load(eq("book_embeddings.json"), eq(Map.class)))
                .thenReturn(Map.of(
                        "1", List.of(1.0, 0.0),
                        "2", List.of(0.5, 0.5)
                ));

        cache = new BookEmbeddingCache(storage, embeddingClient);
    }

    @Test
    void loadsExistingEmbeddings() {
        float[] emb = cache.getEmbedding(new Book(1L, "A", "B"));
        assertNotNull(emb);
        assertEquals(1.0f, emb[0]);
        assertEquals(0.0f, emb[1]);
    }

    @Test
    void addsAndSavesNewEmbedding() {
        Book book = new Book(3L, "New", "Desc");

        when(embeddingClient.embed("Desc"))
                .thenReturn(new float[]{0.2f, 0.8f});

        cache.addOrUpdateBook(book);

        verify(storage, times(1))
                .save(eq("book_embeddings.json"), any());
    }
}
