package com.elibrary.recommendation_service.service;

import com.elibrary.recommendation_service.embedding.EmbeddingClient;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.Recommendation;
import com.elibrary.recommendation_service.similarity.SimilarityCalculator;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationEngine {

    private final FileStorageService storage;
    private final EmbeddingClient embeddingClient;
    private final SimilarityCalculator similarityCalculator;

    public RecommendationEngine(FileStorageService storage,
                                EmbeddingClient embeddingClient,
                                SimilarityCalculator similarityCalculator) {
        this.storage = storage;
        this.embeddingClient = embeddingClient;
        this.similarityCalculator = similarityCalculator;
    }

    public List<Recommendation> recommend(String userId, int limit) {

        List<Map<String, Object>> rawBooks =
                storage.load("books.json", List.class);

        if (rawBooks == null || rawBooks.isEmpty()) {
            return List.of();
        }

        // Convert raw maps to Book objects
        List<Book> books = rawBooks.stream()
                .map(m -> new Book(
                        ((Number)m.get("id")).longValue(),
                        (String)m.get("title"),
                        (String)m.get("description")
                ))
                .toList();

        Map<String, List<String>> loans =
                storage.load("loans.json", Map.class);

        List<String> borrowedIds = loans != null
                ? loans.getOrDefault(userId, List.of())
                : List.of();

        if (borrowedIds.isEmpty()) {
            return fallback(books, limit);
        }

        float[] userEmbedding = buildUserEmbedding(borrowedIds, books);
        if (userEmbedding == null) {
            return fallback(books, limit);
        }

        return books.stream()
                .map(book -> {
                    float[] bookEmbedding = embeddingClient.embed(book.getDescription());
                    if (bookEmbedding == null) return null;

                    double score = similarityCalculator.cosineSimilarity(userEmbedding, bookEmbedding);
                    return new Recommendation(book.getId(), score);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Recommendation::getScore).reversed())
                .limit(limit)
                .toList();
    }

    private float[] buildUserEmbedding(List<String> borrowedIds, List<Book> books) {
        List<float[]> vectors = books.stream()
                .filter(b -> borrowedIds.contains(String.valueOf(b.getId())))
                .map(b -> embeddingClient.embed(b.getDescription()))
                .filter(Objects::nonNull)
                .toList();

        if (vectors.isEmpty()) return null;

        return average(vectors);
    }

    private float[] average(List<float[]> vectors) {
        int size = vectors.get(0).length;
        float[] avg = new float[size];

        for (float[] v : vectors)
            for (int i = 0; i < size; i++)
                avg[i] += v[i];

        for (int i = 0; i < size; i++)
            avg[i] /= vectors.size();

        return avg;
    }

    private List<Recommendation> fallback(List<Book> books, int limit) {
        return books.stream()
                .limit(limit)
                .map(b -> new Recommendation(b.getId(), 0.0))
                .toList();
    }
}


