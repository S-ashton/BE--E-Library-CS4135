package com.elibrary.recommendation_service.service;

import com.elibrary.recommendation_service.embedding.EmbeddingClient;
import com.elibrary.recommendation_service.model.Recommendation;
import com.elibrary.recommendation_service.similarity.SimilarityCalculator;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationEngineTest {

    private FileStorageService storage;
    private EmbeddingClient embeddingClient;
    private SimilarityCalculator similarityCalculator;
    private RecommendationEngine engine;

    @BeforeEach
    void setup() throws Exception {
        storage = new FileStorageService();
        embeddingClient = mock(EmbeddingClient.class);
        similarityCalculator = mock(SimilarityCalculator.class);

        // Copy test files
        Files.copy(Path.of("src/test/resources/books.json"), Path.of("books.json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Path.of("src/test/resources/loans.json"), Path.of("loans.json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        engine = new RecommendationEngine(storage, embeddingClient, similarityCalculator);
    }

    @Test
    void ranksBooksBySimilarity() {
        float[] embMagic = new float[]{1f, 0f};
        float[] embScience = new float[]{0f, 1f};
        float[] userEmb = new float[]{1f, 0f};

        when(embeddingClient.embed("magic story")).thenReturn(embMagic);
        when(embeddingClient.embed("science story")).thenReturn(embScience);

        when(similarityCalculator.cosineSimilarity(userEmb, embMagic)).thenReturn(1.0);
        when(similarityCalculator.cosineSimilarity(userEmb, embScience)).thenReturn(0.0);

        List<Recommendation> recs = engine.recommend("user1", 10);

        assertEquals(1L, recs.get(0).getBookId());
        assertEquals(2L, recs.get(1).getBookId());
    }
}

