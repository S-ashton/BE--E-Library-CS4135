package com.elibrary.recommendation_service.api;

import com.elibrary.recommendation_service.api.dto.RecommendationResponse;
import com.elibrary.recommendation_service.model.Recommendation;
import com.elibrary.recommendation_service.service.RecommendationEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationControllerTest {

    @Test
    void mapsToDto() {
        RecommendationEngine engine = mock(RecommendationEngine.class);

        when(engine.recommend("user1", 5))
                .thenReturn(List.of(new Recommendation(1L, 0.9)));

        RecommendationController controller = new RecommendationController(engine);

        List<RecommendationResponse> result = controller.getRecommendations("user1", 5);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).bookId());
        assertEquals(0.9, result.get(0).score());
    }
}
