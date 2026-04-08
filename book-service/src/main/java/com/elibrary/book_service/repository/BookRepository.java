package com.elibrary.book_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.elibrary.book_service.model.*;


public interface BookRepository extends JpaRepository<Book, Integer>{
    boolean existsByTitleAndAuthorAndYearAndLanguage(String title, String author, int year, Languages language);   

    Book findByIdLong(Long titleId);
}