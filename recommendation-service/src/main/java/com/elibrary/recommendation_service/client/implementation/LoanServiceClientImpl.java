package com.elibrary.recommendation_service.client.implementation;

import com.elibrary.recommendation_service.client.LoanServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class LoanServiceClientImpl implements LoanServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LoanServiceClientImpl(
            RestTemplate restTemplate,
            @Value("${services.loan-service.url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<String> getBorrowedBookIds(String userId) {
        String url = baseUrl + "/api/v1/loans/" + userId;
        String[] response = restTemplate.getForObject(url, String[].class);
        return response == null ? List.of() : Arrays.asList(response);
    }
}
