package com.elibrary.recommendation_service.api.dto;

public record RecommendationResponse(
        long bookId,
        double score
) {}
