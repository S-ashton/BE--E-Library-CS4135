package com.elibrary.book_service.controller;

import com.elibrary.loan_service.dto.BorrowRequestDTO;
import com.elibrary.loan_service.dto.LoanDTO;
import com.elibrary.loan_service.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Endpoints for adding to, altering or requesting from the book catalogue.")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(
            summary = "Add a new title to the catalogue",
            description = "Adds a new title and copy to the database as long as the title does not already exist in the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Title added successfully",
                    content = @Content(schema = @Schema(implementation = AddNewTitleRequest.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)    //TODO: Clarify
    })
    @PostMapping("/addTitle")
    public ResponseEntity<IndividualTitleResponse> addTitle(
            @Valid @RequestBody AddNewTitleRequest request,
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId   //TODO: CHECK FOR LIBRARIAN 
    ) {
        TitleAddedResponse response = bookService.addTitle(userId, request); //TODO: Update
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Add a new copy of an existing title to the catalogue",
            description = "Adds a new copy to the database and triggers the update of copiesAvailable in the Books table"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Copy added successfully",
                    content = @Content(schema = @Schema(implementation = AddNewCopyRequest.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)    //TODO: Clarify
    })
    @PostMapping("/addCopy")
    public ResponseEntity<IndividualCopyResponse> addCopy(
            @Valid @RequestBody AddNewCopyRequest request,
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId   //TODO: CHECK FOR LIBRARIAN 
    ) {
        TitleAddedResponse response = bookService.addCopy(userId, request); //TODO: Update
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Change the status of a copy",
            description = "Changes the status of an existing copy and triggers the update of copiesAvailable in the Books table"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = ChangeCopyStatusRequest.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)    //TODO: Clarify
    })
    @PutMapping("/changeStatus")
    public ResponseEntity<CopyResponse> changeStatus(
            @Valid @RequestBody ChangeCopyStatusRequest request,
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId   //TODO: CHECK FOR LIBRARIAN 
    ) {
        TitleAddedResponse response = bookService.changeStatus(userId, request); //TODO: Update
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Retrieve title",
            description = "Retrieve the details of a single title"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Title details returned successfully",
                    content = @Content(schema = @Schema(implementation = IndividualTitleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)    //TODO: Clarify
    })
    @GetMapping("/{id}")
    public ResponseEntity<TitleAddedResponse> getTitle(@PathVariable("id") Long titleId) {
        TitleAddedResponse response = bookService.getTitle(userId, request); //TODO: Update
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(     //TODO: DTOs need to be added to for this one but endpoint should be correct
            summary = "Retrieve search results",
            description = "Retrieve a list of titles that fit the search criteria"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Title details returned successfully",
                    content = @Content(schema = @Schema(implementation = IndividualTitleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)    //TODO: Clarify
    })
    @GetMapping("/search?keyword={keyword}&genre={genre}&year={year}&language={language}")
    public ResponseEntity<TitleAddedResponse> getTitle(@PathVariable("id") Long titleId) {
        TitleAddedResponse response = bookService.getTitle(userId, request); //TODO: Update
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}