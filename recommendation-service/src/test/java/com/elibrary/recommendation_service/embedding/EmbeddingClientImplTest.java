package com.elibrary.recommendation_service.embedding;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbeddingClientImplTest {

    @Test
    void returnsEmbedding() {
        RestTemplate rest = mock(RestTemplate.class);
        EmbeddingClientImpl client = new EmbeddingClientImpl(rest, "http://localhost:8000");

        float[] mockEmb = new float[]{0.1f, 0.2f};

        when(rest.postForObject("http://localhost:8000/embed", "hello", float[].class))
                .thenReturn(mockEmb);

        assertArrayEquals(mockEmb, client.embed("hello"));
    }

    @Test
    void throwsWhenNull() {
        RestTemplate rest = mock(RestTemplate.class);
        EmbeddingClientImpl client = new EmbeddingClientImpl(rest, "http://localhost:8000");

        when(rest.postForObject("http://localhost:8000/embed", "hello", float[].class))
                .thenReturn(null);

        assertThrows(IllegalStateException.class, () -> client.embed("hello"));
    }
}

