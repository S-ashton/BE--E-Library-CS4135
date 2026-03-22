package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.api.dto.RecommendationResponse;
import com.elibrary.recommendation_service.model.Book;
import com.elibrary.recommendation_service.model.Recommendation;
import com.elibrary.recommendation_service.service.RecommendationEngine;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationEngine engine;

    public RecommendationController(RecommendationEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/{userId}")
    public List<RecommendationResponse> getRecommendations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return engine.recommend(userId, limit).stream()
                .map(r -> new RecommendationResponse(
                        r.getBookId(),
                        r.getScore()
                ))
                .toList();
    }
}
