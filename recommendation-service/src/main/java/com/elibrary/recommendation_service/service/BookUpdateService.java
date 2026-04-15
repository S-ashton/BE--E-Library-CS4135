package com.elibrary.recommendation_service.service;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BookUpdateService {

    private static final String BOOKS_FILE = "data/books.json";

    private final FileStorageService storage;
    private final BookEmbeddingCache embeddingCache;

    public BookUpdateService(FileStorageService storage,
                             BookEmbeddingCache embeddingCache) {
        this.storage = storage;
        this.embeddingCache = embeddingCache;
    }

    public void updateBook(Book book) {
        List<Map<String, Object>> rawBooks = storage.load(BOOKS_FILE, List.class);
        if (rawBooks == null) rawBooks = new ArrayList<>();

        rawBooks.removeIf(existingBook -> sameBookId(existingBook.get("id"), book.getId()));

        Map<String, Object> storedBook = new LinkedHashMap<>();
        storedBook.put("id", book.getId());
        storedBook.put("title", book.getTitle());
        storedBook.put("description", book.getDescription());
        rawBooks.add(storedBook);

        storage.save(BOOKS_FILE, rawBooks);
        embeddingCache.addOrUpdateBook(book);
    }

    public void removeBook(Long bookId) {
        List<Map<String, Object>> rawBooks = storage.load(BOOKS_FILE, List.class);
        if (rawBooks == null) return;

        rawBooks.removeIf(existingBook -> sameBookId(existingBook.get("id"), bookId));
        storage.save(BOOKS_FILE, rawBooks);
        embeddingCache.removeBook(bookId);
    }

    private boolean sameBookId(Object existingId, Long bookId) {
        if (existingId instanceof Number number) {
            return number.longValue() == bookId;
        }

        return Objects.equals(String.valueOf(existingId), String.valueOf(bookId));
    }
}
