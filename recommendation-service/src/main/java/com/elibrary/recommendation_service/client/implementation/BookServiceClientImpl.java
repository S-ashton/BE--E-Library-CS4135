package com.elibrary.recommendation_service.client.implementation;

import com.elibrary.recommendation_service.client.BookServiceClient;
import com.elibrary.recommendation_service.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class BookServiceClientImpl implements BookServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public BookServiceClientImpl(
            RestTemplate restTemplate,
            @Value("${services.book-service.url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<Book> getAllBooks() {
        String url = baseUrl + "/api/v1/books";
        Book[] response = restTemplate.getForObject(url, Book[].class);
        return response == null ? List.of() : Arrays.asList(response);
    }
}