package com.elibrary.book_service.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.elibrary.book_service.model.*;


public interface ElasticsearchRepo extends ElasticsearchRepository<Book, Long> {

    
}