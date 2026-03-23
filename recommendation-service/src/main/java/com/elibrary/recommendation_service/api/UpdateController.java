package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.embedding.BookEmbeddingCache;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/internal")
public class UpdateController {

    private final FileStorageService storage;
    private final BookEmbeddingCache embeddingCache;

    public UpdateController(FileStorageService storage,
                            BookEmbeddingCache embeddingCache) {
        this.storage = storage;
        this.embeddingCache = embeddingCache;
    }

    @PostMapping("/books/update")
    public void updateBook(@RequestBody Book book) {

        List<Map<String, Object>> rawBooks =
                storage.load("data/books.json", List.class);

        if (rawBooks == null) rawBooks = new ArrayList<>();

        // Remove old version
        rawBooks.removeIf(b -> Objects.equals(b.get("id"), book.getId()));

        // Add new version
        rawBooks.add(Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "description", book.getDescription()
        ));

        storage.save("data/books.json", rawBooks);

        // Embed immediately
        embeddingCache.addOrUpdateBook(book);
    }

    @PostMapping("/loans/update")
    public void updateLoan(@RequestBody Map<String, Object> payload) {

        String userId = (String) payload.get("userId");
        String bookId = String.valueOf(payload.get("bookId"));

        Map<String, List<String>> loans =
                storage.load("data/loans.json", Map.class);

        if (loans == null) loans = new HashMap<>();

        loans.computeIfAbsent(userId, k -> new ArrayList<>());

        if (!loans.get(userId).contains(bookId)) {
            loans.get(userId).add(bookId);
        }

        storage.save("data/loans.json", loans);
    }
}

