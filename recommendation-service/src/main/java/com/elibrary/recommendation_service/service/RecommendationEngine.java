package com.elibrary.recommendation_service.service;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.embedding.EmbeddingClient;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.Recommendation;
import com.elibrary.recommendation_service.similarity.SimilarityCalculator;
import com.elibrary.recommendation_service.filtering.CollaborativeFilteringService;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationEngine {

    private final FileStorageService storage;
    private final EmbeddingClient embeddingClient;
    private final SimilarityCalculator similarityCalculator;
    private final CollaborativeFilteringService cfService;
    private final BookEmbeddingCache embeddingCache;

    private static final double ALPHA = 0.7; // content-based weight

    public RecommendationEngine(FileStorageService storage,
                                EmbeddingClient embeddingClient,
                                SimilarityCalculator similarityCalculator,
                                CollaborativeFilteringService cfService,
                                BookEmbeddingCache embeddingCache) {
        this.storage = storage;
        this.embeddingClient = embeddingClient;
        this.similarityCalculator = similarityCalculator;
        this.cfService = cfService;
        this.embeddingCache = embeddingCache;
    }

    public List<Recommendation> recommend(String userId, int limit) {

        List<Map<String, Object>> rawBooks =
                storage.load("data/books.json", List.class);

        if (rawBooks == null || rawBooks.isEmpty()) {
            return List.of();
        }

        List<Book> books = rawBooks.stream()
                .map(m -> new Book(
                        ((Number) m.get("id")).longValue(),
                        (String) m.get("title"),
                        (String) m.get("description")
                ))
                .toList();

        Map<String, List<String>> loans =
                storage.load("data/loans.json", Map.class);

        if (loans == null) loans = new HashMap<>();

        List<String> userLoans = loans.getOrDefault(userId, List.of());

        // No history -> popularity fallback
        if (userLoans.isEmpty()) {
            return popularityFallback(books, loans, limit);
        }

        float[] userEmbedding = buildUserEmbedding(userLoans, books);
        if (userEmbedding == null) {
            return popularityFallback(books, loans, limit);
        }

        // Collaborative filtering scores
        Map<Long, Double> cfScores = cfService.computeScores(userId, loans);

        // Hybrid scoring
        return books.stream()
                .map(book -> {
                    float[] bookEmbedding = embeddingCache.getEmbedding(book);

                    if (bookEmbedding == null) {
                        // lazy embed + cache
                        bookEmbedding = embeddingClient.embed(book.getDescription());
                        embeddingCache.addOrUpdateBook(book);
                    }

                    double contentScore =
                            similarityCalculator.cosineSimilarity(userEmbedding, bookEmbedding);

                    double cfScore = cfScores.getOrDefault(book.getId(), 0.0);

                    double hybridScore = ALPHA * contentScore + (1 - ALPHA) * cfScore;

                    return new Recommendation(book.getId(), hybridScore);
                })
                .sorted(Comparator.comparing(Recommendation::getScore).reversed())
                .limit(limit)
                .toList();
    }

    private float[] buildUserEmbedding(List<String> userLoans, List<Book> books) {
        Map<Long, Book> byId = books.stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        List<float[]> vectors = new ArrayList<>();

        for (String idStr : userLoans) {
            long id = Long.parseLong(idStr);
            Book b = byId.get(id);
            if (b == null) continue;

            float[] emb = embeddingCache.getEmbedding(b);
            if (emb == null) {
                emb = embeddingClient.embed(b.getDescription());
                embeddingCache.addOrUpdateBook(b);
            }
            vectors.add(emb);
        }

        if (vectors.isEmpty()) return null;

        return average(vectors);
    }

    private float[] average(List<float[]> vectors) {
        int size = vectors.getFirst().length;
        float[] avg = new float[size];

        for (float[] v : vectors)
            for (int i = 0; i < size; i++)
                avg[i] += v[i];

        for (int i = 0; i < size; i++)
            avg[i] /= vectors.size();

        return avg;
    }

    private List<Recommendation> popularityFallback(List<Book> books,
                                                    Map<String, List<String>> loans,
                                                    int limit) {

        Map<Long, Integer> borrowCounts = new HashMap<>();

        for (var entry : loans.values()) {
            for (String bookId : entry) {
                long id = Long.parseLong(bookId);
                borrowCounts.merge(id, 1, Integer::sum);
            }
        }

        return books.stream()
                .map(b -> new Recommendation(
                        b.getId(),
                        borrowCounts.getOrDefault(b.getId(), 0)
                ))
                .sorted(Comparator.comparing(Recommendation::getScore).reversed())
                .limit(limit)
                .toList();
    }
}
