package com.elibrary.recommendation_service.embedding;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookEmbeddingCache {

    private final FileStorageService storage;
    private final EmbeddingClient embeddingClient;

    private Map<Long, float[]> cache = new HashMap<>();

    public BookEmbeddingCache(FileStorageService storage,
                              EmbeddingClient embeddingClient) {
        this.storage = storage;
        this.embeddingClient = embeddingClient;
        loadCache();
    }

    @SuppressWarnings("unchecked")
    private void loadCache() {
        Map<String, List<Double>> raw =
                storage.load("data/book_embeddings.json", Map.class);

        if (raw == null) return;

        raw.forEach((id, list) -> {
            float[] arr = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i).floatValue();
            }
            cache.put(Long.valueOf(id), arr);
        });
    }

    public float[] getEmbedding(Book book) {
        return cache.get(book.getId());
    }

    public void addOrUpdateBook(Book book) {
        float[] emb = embeddingClient.embed(book.getDescription());
        cache.put(book.getId(), emb);
        saveCache();
    }

    private void saveCache() {
        Map<String, float[]> raw = new HashMap<>();
        cache.forEach((id, vec) -> raw.put(String.valueOf(id), vec));
        storage.save("data/book_embeddings.json", raw);
    }
}
