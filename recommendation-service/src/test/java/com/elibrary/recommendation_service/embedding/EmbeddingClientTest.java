package com.elibrary.recommendation_service.embedding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingClientTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    RestTemplateBuilder builder;

    EmbeddingClient client;

    @BeforeEach
    void setup() {
        when(builder.rootUri("http://localhost:8000")).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        client = new EmbeddingClient(builder);
    }

    @Test
    void testEmbed() {
        EmbeddingClient.EmbeddingResponse fake = new EmbeddingClient.EmbeddingResponse();
        fake.embedding = new float[]{1.0f, 2.0f};

        when(restTemplate.postForObject(
                eq("/embed"),
                any(),
                eq(EmbeddingClient.EmbeddingResponse.class)
        )).thenReturn(fake);

        float[] result = client.embed("hello");

        assertEquals(2, result.length);
    }
}



