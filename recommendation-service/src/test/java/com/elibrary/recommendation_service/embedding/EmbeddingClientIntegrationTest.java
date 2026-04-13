package com.elibrary.recommendation_service.embedding;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EmbeddingClientIntegrationTest {

    @Autowired
    private Environment env;

    @Autowired
    private EmbeddingClient embeddingClient;

    @Test
    void debugProperties() {
        System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("Embedding URL: " + env.getProperty("services.embedding-service.url"));
    }

    @Test
    void testEmbeddingServiceCommunication() {
        float[] embedding = embeddingClient.embed("Hello world");

        assertNotNull(embedding);
        assertTrue(embedding.length > 100);
    }
}


