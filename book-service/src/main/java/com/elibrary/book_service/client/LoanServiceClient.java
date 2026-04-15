package com.elibrary.book_service.client;

import com.elibrary.book_service.exceptions.ActiveLoansExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class LoanServiceClient {

    private static final Logger log = LoggerFactory.getLogger(LoanServiceClient.class);

    private final String loanServiceBaseUrl;
    private final RestTemplate restTemplate;

    public LoanServiceClient(
            @Value("${services.loan.base-url:http://loan-service:8083}") String loanServiceBaseUrl,
            RestTemplate restTemplate
    ) {
        this.loanServiceBaseUrl = loanServiceBaseUrl;
        this.restTemplate = restTemplate;
    }

    public void checkNoActiveLoans(Long bookId) {
        String url = loanServiceBaseUrl + "/api/loans/has-active-by-book?bookId=" + bookId;
        try {
            Boolean hasActive = restTemplate.getForObject(url, Boolean.class);
            if (Boolean.TRUE.equals(hasActive)) {
                throw new ActiveLoansExistException(
                        "Cannot delete book: there are active or overdue loans on this book");
            }
        } catch (ActiveLoansExistException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.warn("Could not reach loan-service to check active loans for book {}: {}", bookId, ex.getMessage());
        }
    }
}
