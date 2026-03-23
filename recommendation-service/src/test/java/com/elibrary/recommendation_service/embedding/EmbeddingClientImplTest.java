package com.elibrary.recommendation_service.embedding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmbeddingClientImplTest {

    @Mock
    RestTemplate restTemplate;

    @Test
    void returnsEmbedding() {
        String baseUrl = "http://localhost:8000";
        EmbeddingClientImpl client = new EmbeddingClientImpl(restTemplate, baseUrl);

        Map<String, Object> fakeResponse = Map.of(
                "embedding", List.of(0.1, 0.2, 0.3)
        );

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(fakeResponse);

        float[] result = client.embed("hello");

        assertEquals(3, result.length);
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

