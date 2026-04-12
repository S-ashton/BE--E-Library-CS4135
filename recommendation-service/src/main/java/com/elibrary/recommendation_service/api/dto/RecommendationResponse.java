package com.elibrary.recommendation_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object representing a recommended book and its relevance score")
public record RecommendationResponse(
        @Schema(description = "ID of the recommended book", example = "39")
        long bookId,
        @Schema(description = "Relevance score between 0 and 1", example = "0.87")
        double score
) {}
