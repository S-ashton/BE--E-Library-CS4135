package com.elibrary.recommendation_service.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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

        Map<String, String> request = Map.of("text", text);

        Map response = restTemplate.postForObject(url, request, Map.class);

        if (response == null || !response.containsKey("embedding")) {
            throw new IllegalStateException("Embedding service returned invalid response");
        }

        List<Double> list = (List<Double>) response.get("embedding");
        float[] embedding = new float[list.size()];

        for (int i = 0; i < list.size(); i++) {
            embedding[i] = list.get(i).floatValue();
        }

        return embedding;
    }
}

