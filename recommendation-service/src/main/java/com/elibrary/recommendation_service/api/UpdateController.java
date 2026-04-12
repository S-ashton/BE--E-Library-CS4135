package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.LoanRecord;
import com.elibrary.recommendation_service.service.BookUpdateService;
import com.elibrary.recommendation_service.storage.FileStorageService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@RestController
@RequestMapping("/api/recommendations/internal")
@Tag(
        name = "Internal Updates",
        description = "Endpoints used by book-service and loan-service to push updates"
)
public class UpdateController {

    private final FileStorageService storage;
    private final BookUpdateService bookUpdateService;

    public UpdateController(FileStorageService storage,
                            BookUpdateService bookUpdateService) {
        this.storage = storage;
        this.bookUpdateService = bookUpdateService;
    }

    @Operation(
            summary = "Update or insert a book",
            description = "Receives a book object from book-service and updates the local cache and embeddings."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid book payload", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @PostMapping("/books/update")
    public void updateBook(@RequestBody
                               @Schema(description = "Book object containing updated metadata")
                               Book book)
    {
        bookUpdateService.updateBook(book);
    }

    @Operation(
            summary = "Update loan history",
            description = "Receives a loan event from loan-service and updates the local loan cache."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid loan payload", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @PostMapping("/loans/update")
    public void updateLoan(
            @RequestBody
            @Schema(description = "Loan event payload sent by loan-service")
            LoanRecord payload
    ) {
        String userId = String.valueOf(payload.getUserId());
        String bookId = String.valueOf(payload.getBookId());

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

