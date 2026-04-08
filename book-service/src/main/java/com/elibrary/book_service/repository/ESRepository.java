package com.elibrary.book_service.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.elibrary.book_service.model.*;

public interface ESRepository extends ElasticsearchRepository<Book, String> {

    
}