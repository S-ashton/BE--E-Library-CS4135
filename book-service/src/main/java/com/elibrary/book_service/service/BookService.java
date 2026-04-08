package com.elibrary.book_service.service;

import com.elibrary.book_service.dto.*;
import com.elibrary.book_service.repository.BookRepository;
import com.elibrary.book_service.repository.CopyRepository;
import com.elibrary.book_service.repository.ESRepository;
import com.elibrary.book_service.model.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;

import jakarta.transaction.Transactional;

public class BookService {

    private final BookRepository bookRepository;
    private final CopyRepository copyRepository;
    private final ESRepository esRepository;

    public BookService(BookRepository bookRepository, CopyRepository copyRepository, ESRepository esRepository){
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.esRepository = esRepository;
    }

    //search

    @Transactional
    public TitleResponseDTO addTitle(TitleRequestDTO title){
        boolean duplicateExists = bookRepository.existsByTitleAndAuthorAndYearAndLanguage(
            title.getTitle(),
            title.getAuthor(), 
            title.getYearPublished(), 
            title.getLanguage()
        );

        if(duplicateExists){
            throw new TitleAlreadyExistsException("This title already exists in the system, please add another copy instead");  //TODO: just replace with adding another copy instead?
        }

        Book newTitle = new Book(title.getTitle(),
            title.getAuthor(), 
            title.getDescription(),
            title.getYearPublished(), 
            title.getGenre(),
            title.getCoverImage(),
            title.getLanguage());

        bookRepository.save(newTitle);
        esRepository.save(newTitle);

        copyRepository.save(new BookCopy(newTitle.getId(), Status.AVAILABLE));

        //TODO: Send notification to Recommender

        return newTitle.toDto();
    }

    @Transactional
    public CopyResponseDTO addCopy(Long bookId){
        BookCopy newCopy = new BookCopy(bookId, Status.AVAILABLE).orElseThrow(
            TitleNotFoundException::new  //TODO: Implement exception
        );

        copyRepository.save(newCopy);

        int currentNumCopies = copyRepository.countByIdAndAvailability(bookId, Status.AVAILABLE);
        Book book = bookRepository.findByIdLong(bookId);
        book.setCopiesAvailable(currentNumCopies);
        bookRepository.save(book);
        esRepository.save(book);

        return newCopy.toDto();
    }

    @Transactional
    public CopyResponseDTO changeStatus(Long copyId, Status status){
        BookCopy currentCopy = copyRepository.findByIdLong(copyId).orElseThrow(
            CopyNotFoundException::new  //TODO: Implement exception
        );

        if(currentCopy.getStatus().equals(status)){
            throw new statusMatchingException;  //TODO: Implement exception
        }else{
            currentCopy.setStatus(status);
            currentCopy = copyRepository.save(currentCopy);

            int currentNumCopies = copyRepository.countByIdAndAvailability(currentCopy.getBookId(), Status.AVAILABLE);
            Book book = bookRepository.findByIdLong(currentCopy.getBookId());
            book.setCopiesAvailable(currentNumCopies);
            bookRepository.save(book);
            esRepository.save(book);

        }

        return currentCopy.toDto();

    }

    @Transactional
    public TitleResponseDTO getTitle(Long titleId){
        Book title = bookRepository.findByIdLong(titleId).orElseThrow(
            TitleNotFoundException::new //TODO: Implement exception
        );

        return title.toDto();

    }

    @Transactional
    public List<TitleResponseDTO> search(String keyword, Genre genre, int year, Languages language){
        
    }
    }
}
