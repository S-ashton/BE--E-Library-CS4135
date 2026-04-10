package com.elibrary.book_service.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.elibrary.book_service.model.*;


public interface TitleRepository extends JpaRepository<Book, Integer>{
    boolean existsByTitleAndAuthorAndYearPublishedAndLanguage(String title, String author, int yearPublished, Languages language);   

    Optional findByTitleAndAuthorAndYearPublishedAndLanguage(String title, String author, int yearPublished, Languages language);

    Optional<Book> findById(Long titleId);
}