package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.api.dto.RecommendationResponse;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.Recommendation;
import com.elibrary.recommendation_service.service.RecommendationEngine;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Endpoints for generating book recommendations")
public class RecommendationController {

    private final RecommendationEngine engine;

    public RecommendationController(RecommendationEngine engine) {
        this.engine = engine;
    }

    @Operation(
            summary = "Get recommendations for a user",
            description = "Returns a ranked list of recommended books for the given user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommendations generated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RecommendationResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public RecommendationResponse getRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("X-User-Id") String userId
    ) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be at least 1");
        }

        List<Recommendation> recs = engine.recommend(userId, limit);

        if (recs == null || recs.isEmpty()) {
            throw new EntityNotFoundException("No recommendations found for user: " + userId);
        }

        return new RecommendationResponse(recs);
    }

}
