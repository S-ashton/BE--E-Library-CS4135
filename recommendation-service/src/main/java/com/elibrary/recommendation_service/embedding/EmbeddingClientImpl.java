package com.elibrary.recommendation_service.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmbeddingClientImpl implements EmbeddingClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EmbeddingClientImpl(
            RestTemplate restTemplate,
            @Value("${services.embedding-service.url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public float[] embed(String text) {
        String url = baseUrl + "/embed";
        float[] embedding = restTemplate.postForObject(url, text, float[].class);

        if (embedding == null) {
            throw new IllegalStateException("Embedding service returned null");
        }

        return embedding;
    }
}
