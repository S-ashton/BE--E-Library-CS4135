package com.elibrary.recommendation_service.client;

import com.elibrary.recommendation_service.model.Book;

import java.util.List;

public interface BookServiceClient {

    List<Book> getAllBooks();
}