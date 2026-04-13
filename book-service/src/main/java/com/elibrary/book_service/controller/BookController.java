package com.elibrary.book_service.controller;

import com.elibrary.book_service.dto.*;
import com.elibrary.book_service.model.*;
import com.elibrary.book_service.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
                    content = @Content(schema = @Schema(implementation = TitleRequestDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)    //TODO: Clarify
    })
    @PostMapping(value = "/addTitle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TitleResponseDTO> addTitle(
            @Valid @ModelAttribute TitleRequestDTO request,
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId   //TODO: CHECK FOR LIBRARIAN 
    ) {
        TitleResponseDTO response = bookService.addTitle(request); //TODO: Update
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Add a new copy of an existing title to the catalogue",
            description = "Adds a new copy to the database and triggers the update of copiesAvailable in the Books table"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Copy added successfully",
                    content = @Content(schema = @Schema(implementation = CopyCreationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    @PostMapping("/addCopy")
    public ResponseEntity<CopyResponseDTO> addCopy(
            @Valid @RequestParam Long titleId,
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId
    ) {
        CopyResponseDTO response = bookService.addCopy(titleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Change the status of a copy",
            description = "Changes the status of an existing copy and triggers the update of copiesAvailable in the Books table"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = CopyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    @PutMapping("/changeStatus")
    public ResponseEntity<CopyResponseDTO> changeStatus(
            @Valid @RequestParam Long copyId,
            @Valid @RequestParam Status status,
            @Parameter(description = "Trusted identity header forwarded by gateway")
            @RequestHeader("X-Authenticated-User-Id") Long userId 
    ) {
        CopyResponseDTO response = bookService.changeStatus(copyId, status);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Retrieve title",
            description = "Retrieve the details of a single title"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Title details returned successfully",
                    content = @Content(schema = @Schema(implementation = TitleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content) 
    })
    @GetMapping("/{id}")
    public ResponseEntity<TitleResponseDTO> getTitle(@PathVariable("id") Long titleId) {
        TitleResponseDTO response = bookService.getTitle(titleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Retrieve search results",
            description = "Retrieve a list of titles that fit the search criteria"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Title details returned successfully",
                    content = @Content(schema = @Schema(implementation = TitleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content) 
    })
    @GetMapping("/search")
    public ResponseEntity<List<TitleResponseDTO>> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Genre genre,
        @RequestParam(defaultValue = "0") int year,
        @RequestParam(required = false) Languages language
    ) throws IOException {
        List<TitleResponseDTO> response = bookService.search(keyword, genre, year, language);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Retrieve a list of titles from ids",
        description = "When given a list of title ids, retrieve the relevant objects from the DB and return them"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Title details returned successfully",
                    content = @Content(schema = @Schema(implementation = TitleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid user identity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    @GetMapping("/titlesByIds")
    public ResponseEntity<List<TitleResponseDTO>> titlesFromIds(
        @RequestParam List<Long> bookIds
    ){
        List<TitleResponseDTO> response = bookService.titlesByIds(bookIds);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Retrieve an available copy of a title",
        description = "Find an available copy of a title from the given title id and return its details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Copy details returned successfully",
                    content = @Content(schema = @Schema(implementation = CopyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    @GetMapping("/getAvailableCopy")
    public ResponseEntity<CopyResponseDTO> getAvailableCopy(
        @RequestParam Long bookId
    ) throws IOException{
        CopyResponseDTO response = bookService.getAvailableCopy(bookId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Find how many copies have a certain status",
        description = "Count the number of copies of a title with the given status, or the total number of copies of that title if no status is given"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Copy count returned successfully",
                    content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    @GetMapping("/countCopies")
    public ResponseEntity<Integer> countCopies(
        @RequestParam Long bookId,
        @RequestParam(required = false) Status status
    ){
        Integer response = bookService.countCopies(bookId, status);
        return ResponseEntity.ok(response);
    }
}