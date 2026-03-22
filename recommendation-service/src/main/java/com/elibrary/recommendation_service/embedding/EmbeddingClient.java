package com.elibrary.recommendation_service.embedding;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Map;

@Service
public class EmbeddingClient {

    private final RestTemplate restTemplate;

    public EmbeddingClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .rootUri("http://localhost:8000") // python service
                .build();
    }

    public float[] embed(String text) {
        Map<String, String> request = Map.of("text", text);

        EmbeddingResponse response = restTemplate.postForObject(
                "/embed",
                request,
                EmbeddingResponse.class
        );

        if (response == null || response.embedding == null) {
            throw new IllegalStateException("Embedding service returned null");
        }

        return response.embedding;
    }

    public static class EmbeddingResponse {
        public float[] embedding;
    }
}
