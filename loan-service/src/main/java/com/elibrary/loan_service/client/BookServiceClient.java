package com.elibrary.loan_service.client;

import com.elibrary.loan_service.exception.BookServiceUnavailableException;
import com.elibrary.loan_service.exception.BookUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class BookServiceClient {

    private static final Logger log = LoggerFactory.getLogger(BookServiceClient.class);
    private static final String CIRCUIT_BREAKER_NAME = "book-service";

    private final String gatewayBaseUrl;
    private final String bookServiceBaseUrl;
    private final RestTemplate restTemplate;

    public BookServiceClient(
            @Value("${services.gateway.base-url:http://localhost:8080}") String gatewayBaseUrl,
            @Value("${services.book.base-url:http://localhost:8082}") String bookServiceBaseUrl,
            RestTemplate restTemplate
    ) {
        this.gatewayBaseUrl = gatewayBaseUrl;
        this.bookServiceBaseUrl = bookServiceBaseUrl;
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getAvailableCopyFallback")
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
            throw new BookUnavailableException("No available copy found for book " + bookId);

        } catch (HttpClientErrorException.Conflict ex) {
            throw new BookUnavailableException("No available copy found for book " + bookId);

        } catch (HttpClientErrorException.BadRequest ex) {
            throw new BookUnavailableException("No available copy found for book " + bookId);

        } catch (HttpServerErrorException ex) {
            String body = ex.getResponseBodyAsString();
            log.warn("Book service 5xx while getting available copy for book {}: {}", bookId, body);

            if (body != null) {
                String lower = body.toLowerCase();
                if (lower.contains("no title with this id exists")
                        || lower.contains("title not found")
                        || lower.contains("no available copy")
                        || lower.contains("copy not found")) {
                    throw new BookUnavailableException("No available copy found for book " + bookId);
                }
            }

            throw new BookServiceUnavailableException(
                    "Book service is currently unavailable. Please try again later.", ex);

        } catch (RestClientException ex) {
            log.warn("Book service request failed while getting available copy for book {}: {}", bookId, ex.getMessage());
            throw new BookServiceUnavailableException(
                    "Book service is currently unavailable. Please try again later.", ex);
        }
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "changeCopyStatusFallback")
    public void changeCopyStatus(Long copyId, String status, Long userId, String authorization) {
        String url = bookServiceBaseUrl + "/api/books/changeStatus?copyId=" + copyId + "&status=" + status;

        HttpHeaders headers = new HttpHeaders();

        headers.set("X-Authenticated-User", "loan-service");
        headers.set("X-Authenticated-Role", "STAFF");
        headers.set("X-Authenticated-User-Id", String.valueOf(userId));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );

        } catch (HttpClientErrorException.NotFound ex) {
            throw new BookUnavailableException("Book copy not found: " + copyId);

        } catch (HttpClientErrorException.Conflict ex) {
            throw new BookUnavailableException("Book copy status could not be updated for copy " + copyId);

        } catch (HttpClientErrorException.BadRequest ex) {
            throw new BookUnavailableException("Book copy status could not be updated for copy " + copyId);

        } catch (HttpServerErrorException ex) {
            log.warn("Book service 5xx while changing copy status. copyId={}, status={}, body={}",
                    copyId, status, ex.getResponseBodyAsString());
            throw new BookServiceUnavailableException(
                    "Book service is currently unavailable. Could not update book copy status.", ex);

        } catch (RestClientException ex) {
            log.warn("Book service request failed while changing copy status. copyId={}, status={}, error={}",
                    copyId, status, ex.getMessage());
            throw new BookServiceUnavailableException(
                    "Book service is currently unavailable. Could not update book copy status.", ex);
        }
    }

    private BookCopyResponseDTO getAvailableCopyFallback(Long bookId, String authorization, Throwable t) {
        if (t instanceof BookUnavailableException ex) {
            throw ex;
        }

        log.warn("Circuit breaker open for book-service (getAvailableCopy, bookId={}): {}", bookId, t.getMessage());
        throw new BookServiceUnavailableException(
                "Book service is currently unavailable. Please try again later.", t);
    }

    private void changeCopyStatusFallback(Long copyId, String status, Long userId, String authorization, Throwable t) {
        if (t instanceof BookUnavailableException ex) {
            throw ex;
        }

        log.warn("Circuit breaker open for book-service (changeCopyStatus, copyId={}, status={}): {}",
                copyId, status, t.getMessage());
        throw new BookServiceUnavailableException(
                "Book service is currently unavailable. Could not update book copy status.", t);
    }
}