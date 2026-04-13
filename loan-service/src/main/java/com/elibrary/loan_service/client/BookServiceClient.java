package com.elibrary.loan_service.client;

import com.elibrary.loan_service.exception.BookUnavailableException;
import com.elibrary.loan_service.exception.LoanNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class BookServiceClient {

    private final String gatewayBaseUrl;
    private final String bookServiceDirectUrl;
    private final RestTemplate restTemplate;

    public BookServiceClient(
            @Value("${services.gateway.base-url:http://localhost:8080}") String gatewayBaseUrl,
            @Value("${services.book.base-url:http://localhost:8082}") String bookServiceDirectUrl,
            RestTemplate restTemplate
    ) {
        this.gatewayBaseUrl = gatewayBaseUrl;
        this.bookServiceDirectUrl = bookServiceDirectUrl;
        this.restTemplate = restTemplate;
    }

    public BookCopyResponseDTO getAvailableCopy(Long bookId, String authorization) {
        String url = gatewayBaseUrl + "/api/books/getAvailableCopy?bookId=" + bookId;

        HttpHeaders headers = new HttpHeaders();
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<BookCopyResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    BookCopyResponseDTO.class
            );

            BookCopyResponseDTO body = response.getBody();
            if (body == null) {
                throw new BookUnavailableException("No available copy found for book " + bookId);
            }

            return body;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new LoanNotFoundException("Book title not found");
        } catch (HttpClientErrorException.Conflict ex) {
            throw new BookUnavailableException("No available copy found for book " + bookId);
        }
    }

    public void changeCopyStatus(Long copyId, String status, Long userId, String authorization) {
        String url = bookServiceDirectUrl + "/api/books/changeStatus?copyId=" + copyId + "&status=" + status;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authenticated-User", "loan-service");
        headers.set("X-Authenticated-Role", "STAFF");
        headers.set("X-Authenticated-User-Id", String.valueOf(userId));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );
    }
}