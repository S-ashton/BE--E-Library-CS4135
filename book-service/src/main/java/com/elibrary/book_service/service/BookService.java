package com.elibrary.book_service.service;

import com.elibrary.book_service.dto.*;
import com.elibrary.book_service.repository.elasticsearch.ElasticsearchRepo;
import com.elibrary.book_service.repository.jpa.CopyRepository;
import com.elibrary.book_service.repository.jpa.TitleRepository;

import co.elastic.clients.elasticsearch.core.SearchRequest;

import com.elibrary.book_service.model.*;
import com.elibrary.book_service.exceptions.*;
import com.elibrary.book_service.messaging.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final TitleRepository bookRepository;
    private final CopyRepository copyRepository;
    private final ElasticsearchRepo esRepository;
    private final ElasticsearchClient esClient;
    private final BookEventPublisher bookEventPublisher;
    private final MinioService minioService;

    public BookService(TitleRepository bookRepository, CopyRepository copyRepository, ElasticsearchRepo esRepository, ElasticsearchClient esClient, BookEventPublisher bookEventPublisher, MinioService minioService){
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.esRepository = esRepository;
        this.esClient = esClient;
        this.bookEventPublisher = bookEventPublisher;
        this.minioService = minioService;
    }

    //search

    @Transactional
    public TitleResponseDTO addTitle(TitleRequestDTO title){
        bookRepository.findByTitleAndAuthorAndYearPublishedAndLanguage(
            title.getTitle(),
            title.getAuthor(),
            title.getYearPublished(),
            title.getLanguage()
        ).ifPresent(existing -> {
            throw new TitleAlreadyExistsException(
                "This title already exists in the system, please add another copy instead",
                existing.getId()
            );
        });

        String coverImageUrl;
        try {
            coverImageUrl = minioService.uploadCoverImage(title.getCoverImage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload cover image", e);
        }

        Book newTitle = new Book(title.getTitle(),
            title.getAuthor(), 
            title.getDescription(),
            title.getYearPublished(), 
            title.getGenre(),
            coverImageUrl,
            title.getLanguage());

        bookRepository.save(newTitle);
        esRepository.save(newTitle);

        copyRepository.save(new BookCopy(newTitle.getId(), Status.AVAILABLE));

        try {
            BookAddedEvent event = new BookAddedEvent(
                newTitle.getId(),
                newTitle.getTitle(),
                newTitle.getAuthor(), 
                newTitle.getDescription(),
                newTitle.getYearPublished(), 
                newTitle.getGenre(),
                newTitle.getCoverImageUrl(),
                newTitle.getLanguage(),
                1
            );
            bookEventPublisher.publishBookAdded(event);
        } catch (Exception ex) {
            log.warn("loan.borrowed publish failed for loanId={}", newTitle.getId(), ex);
        }

        return newTitle.toDto();
    }

    @Transactional
    public CopyResponseDTO addCopy(Long bookId){
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new TitleNotFoundException("No title with this ID exists"));

        BookCopy newCopy = new BookCopy(bookId, Status.AVAILABLE);

        copyRepository.save(newCopy);

        int currentNumCopies = copyRepository.countByIdAndStatus(bookId, Status.AVAILABLE);
        book.setCopiesAvailable(currentNumCopies);
        bookRepository.save(book);
        esRepository.save(book);

        return newCopy.toDto();
    }

    @Transactional
    public CopyResponseDTO changeStatus(Long copyId, Status status){
        BookCopy currentCopy = copyRepository.findById(copyId)
            .orElseThrow(() -> new CopyNotFoundException("No copy with this ID exists")
        );

        if(currentCopy.getStatus().equals(status)){
            throw new StatusMatchingException("This copy already has this status");
        }else{
            currentCopy.setStatus(status);
            currentCopy = copyRepository.save(currentCopy);

            int currentNumCopies = copyRepository.countByIdAndStatus(currentCopy.getBookId(), Status.AVAILABLE);
            Book book = bookRepository.findById(currentCopy.getBookId()).get();
            book.setCopiesAvailable(currentNumCopies);
            bookRepository.save(book);
            esRepository.save(book);

        }

        return currentCopy.toDto();

    }

    @Transactional
    public TitleResponseDTO getTitle(Long titleId){
        Book title = bookRepository.findById(titleId)
            .orElseThrow(() -> new TitleNotFoundException("No title with this ID exists")
        );

        return title.toDto();

    }

    @Transactional
    public List<TitleResponseDTO> search(String keyword, Genre genre, Integer year, Languages language) throws IOException {

        // Build filter clauses
        List<Query> filters = new ArrayList<>();

        if (genre != null) {
            filters.add(Query.of(q -> q
                .term(t -> t
                    .field("genre")
                    .value(genre.name())
                )
            ));
        }

        if (language != null) {
            filters.add(Query.of(q -> q
                .term(t -> t
                    .field("language")
                    .value(language.name())
                )
            ));
        }

        if (year != null && year != 0) {
            filters.add(Query.of(q -> q
                .term(t -> t
                    .field("yearPublished")
                    .value(year)
                )
            ));
        }

        Query mainQuery;
        if (keyword != null && !keyword.isBlank()) {
            mainQuery = Query.of(q -> q
                .multiMatch(mm -> mm
                    .query(keyword)
                    .fields("title^3", "author^2", "description^1")
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
                )
            );
        } else {
            mainQuery = Query.of(q -> q.matchAll(m -> m));
        }

        // Combine into a bool query
        Query finalQuery;
        if (filters.isEmpty()) {
            finalQuery = mainQuery;
        } else {
            Query capturedMain = mainQuery;
            finalQuery = Query.of(q -> q
                .bool(b -> b
                    .must(capturedMain)
                    .filter(filters)
                )
            );
        }

        Query capturedFinal = finalQuery;
        SearchRequest request = SearchRequest.of(r -> r
            .index("books")
            .query(capturedFinal)
        );

        SearchResponse<Book> results = esClient.search(request, Book.class);

        List<TitleResponseDTO> books = results.hits()
                          .hits()
                          .stream()
                          .map(Hit::source)
                          .filter(Objects::nonNull)
                          .map(Book::toDto)
                          .collect(Collectors.toList());

        return books;
    }

    @Transactional
    public List<TitleResponseDTO> titlesByIds(List<Long> bookIds){
        List<TitleResponseDTO> books = new ArrayList<>();
        for (Long num : bookIds) {      //TODO: Add exception for non-existent title/id
            Book book = bookRepository.findById(num)
                .orElseThrow(() -> new TitleNotFoundException("No title with this ID exists"));
            books.add(book.toDto());
        }
        return books;
    }

    @Transactional
    public CopyResponseDTO getAvailableCopy(Long bookId) throws IOException {
        List<BookCopy> availableCopies = copyRepository.findByBookIdAndStatus(bookId, Status.AVAILABLE)
            .orElse(java.util.Collections.emptyList());
        if (availableCopies.isEmpty()) {
            throw new CopyNotFoundException("There are no available copies of this title");
        }
        return availableCopies.get(0).toDto();
    }

    @Transactional
    public Integer countCopies(Long bookId, Status status){
        int countCopies;

        if(status!=null){
            List<BookCopy> copies = copyRepository.findByBookIdAndStatus(bookId, status)
                .orElseThrow(() -> new CopyNotFoundException("No copies of this title with this status exist"));
            countCopies = copies.size();
        }else{
            List<BookCopy> copies = copyRepository.findByBookId(bookId)
                .orElseThrow(() -> new CopyNotFoundException("There are no copies of this title"));
            countCopies = copies.size();
        }

        return countCopies;
    }
}
