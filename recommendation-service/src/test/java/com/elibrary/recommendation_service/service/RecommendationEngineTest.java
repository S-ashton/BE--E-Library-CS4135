package com.elibrary.recommendation_service.service;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.embedding.EmbeddingClient;
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

        when(storage.load(eq("data/books.json"), eq(List.class)))
                .thenReturn(List.of(
                        Map.of("id", 1, "title", "Magic",   "description", "magic story"),
                        Map.of("id", 2, "title", "Fantasy", "description", "fantasy story"),
                        Map.of("id", 3, "title", "Science", "description", "science story")
                ));

        when(storage.load(eq("data/loans.json"), eq(Map.class)))
                .thenReturn(Map.of("user1", List.of("1")));

        float[] embMagic   = new float[]{1f, 0f};
        float[] embFantasy = new float[]{1f, 0f};
        float[] embScience = new float[]{0f, 1f};

        when(embeddingCache.getEmbedding(argThat(b -> b != null && b.getId() == 1L))).thenReturn(embMagic);
        when(embeddingCache.getEmbedding(argThat(b -> b != null && b.getId() == 2L))).thenReturn(embFantasy);
        when(embeddingCache.getEmbedding(argThat(b -> b != null && b.getId() == 3L))).thenReturn(embScience);

        when(similarityCalculator.cosineSimilarity(embMagic, embFantasy)).thenReturn(1.0);
        when(similarityCalculator.cosineSimilarity(embMagic, embScience)).thenReturn(0.0);

        when(cfService.computeScores(eq("user1"), any()))
                .thenReturn(Map.of(3L, 0.5));

        List<Recommendation> recs = engine.recommend("user1", 10);

        assertEquals(2, recs.size());
        assertEquals(2L, recs.get(0).getBookId());
        assertEquals(3L, recs.get(1).getBookId());
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


