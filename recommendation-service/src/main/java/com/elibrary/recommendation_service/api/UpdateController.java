package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/internal")
public class UpdateController {

    private final FileStorageService storage;

    public UpdateController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/books/update")
    public void updateBook(@RequestBody Book book) {
        List<Book> books = storage.load("books.json", List.class);
        if (books == null) books = new ArrayList<>();

        // Remove old version if exists
        books.removeIf(b -> Objects.equals(((Map)b).get("id"), book.getId()));

        // Add new version
        books.add(book);

        storage.save("books.json", books);
    }

    @PostMapping("/loans/update")
    public void updateLoan(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String bookId = String.valueOf(payload.get("bookId"));

        Map<String, List<String>> loans = storage.load("loans.json", Map.class);
        if (loans == null) loans = new HashMap<>();

        loans.computeIfAbsent(userId, k -> new ArrayList<>());

        // Add book to user's loan list
        if (!loans.get(userId).contains(bookId)) {
            loans.get(userId).add(bookId);
        }

        storage.save("loans.json", loans);
    }
}
