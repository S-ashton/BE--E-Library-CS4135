package com.elibrary.recommendation_service.embedding;

public interface EmbeddingClient {
    float[] embed(String text);
}

