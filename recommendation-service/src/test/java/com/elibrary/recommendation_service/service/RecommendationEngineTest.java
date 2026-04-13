package com.elibrary.recommendation_service.service;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.embedding.EmbeddingClient;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.Recommendation;
import com.elibrary.recommendation_service.similarity.SimilarityCalculator;
import com.elibrary.recommendation_service.filtering.CollaborativeFilteringService;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationEngineTest {

    private FileStorageService storage;
    private EmbeddingClient embeddingClient;
    private SimilarityCalculator similarityCalculator;
    private CollaborativeFilteringService cfService;
    private BookEmbeddingCache embeddingCache;
    private RecommendationEngine engine;

    @BeforeEach
    void setup() {
        storage = mock(FileStorageService.class);
        embeddingClient = mock(EmbeddingClient.class);
        similarityCalculator = mock(SimilarityCalculator.class);
        cfService = mock(CollaborativeFilteringService.class);
        embeddingCache = mock(BookEmbeddingCache.class);

        engine = new RecommendationEngine(
                storage, embeddingClient, similarityCalculator, cfService, embeddingCache
        );
    }

    @Test
    void usesHybridScoring() {

        // Mock books.json
        when(storage.load(eq("data/books.json"), eq(List.class)))
                .thenReturn(List.of(
                        Map.of("id", 1, "title", "Magic", "description", "magic story"),
                        Map.of("id", 2, "title", "Science", "description", "science story")
                ));

        // Mock loans.json
        when(storage.load(eq("data/loans.json"), eq(Map.class)))
                .thenReturn(Map.of("user1", List.of("1")));

        // Expected Book instances
        Book magic = new Book(1L, "Magic", "magic story");
        Book science = new Book(2L, "Science", "science story");

        // Embeddings
        float[] embMagic = new float[]{1f, 0f};
        float[] embScience = new float[]{0f, 1f};

        // Null‑safe matchers
        when(embeddingCache.getEmbedding(argThat(b -> b != null && b.getId() == 1L)))
                .thenReturn(embMagic);

        when(embeddingCache.getEmbedding(argThat(b -> b != null && b.getId() == 2L)))
                .thenReturn(embScience);

        // User embedding is built from book 1
        when(embeddingCache.getEmbedding(argThat(b -> b != null && b.getId() == 1L)))
                .thenReturn(embMagic);

        // Content similarity
        when(similarityCalculator.cosineSimilarity(embMagic, embMagic)).thenReturn(1.0);
        when(similarityCalculator.cosineSimilarity(embMagic, embScience)).thenReturn(0.0);

        // CF score: book 2 gets a boost
        when(cfService.computeScores(eq("user1"), any()))
                .thenReturn(Map.of(2L, 0.5));

        List<Recommendation> recs = engine.recommend("user1", 10);

        assertEquals(1L, recs.get(0).getBookId()); // Magic first
        assertEquals(2L, recs.get(1).getBookId()); // Science second
    }

    @Test
    void popularityFallbackWhenNoLoans() {

        when(storage.load(eq("data/books.json"), eq(List.class)))
                .thenReturn(List.of(
                        Map.of("id", 1, "title", "A", "description", "x"),
                        Map.of("id", 2, "title", "B", "description", "y")
                ));

        when(storage.load(eq("data/loans.json"), eq(Map.class)))
                .thenReturn(Map.of());

        List<Recommendation> recs = engine.recommend("userX", 10);

        assertEquals(2, recs.size());
    }
}


