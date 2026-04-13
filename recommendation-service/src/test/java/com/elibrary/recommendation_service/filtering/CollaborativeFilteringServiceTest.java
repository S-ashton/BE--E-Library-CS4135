package com.elibrary.recommendation_service.filtering;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CollaborativeFilteringServiceTest {

    private final CollaborativeFilteringService service = new CollaborativeFilteringService();

    @Test
    void computesCollaborativeScores() {
        Map<String, List<String>> loans = Map.of(
                "user1", List.of("1", "2"),
                "user2", List.of("2", "3"),
                "user3", List.of("1", "3", "4")
        );

        Map<Long, Double> scores = service.computeScores("user1", loans);

        // user1 borrowed 1,2 -> should recommend 3 and 4
        assertTrue(scores.containsKey(3L));
        assertTrue(scores.containsKey(4L));

        // Should NOT recommend books user1 already borrowed
        assertFalse(scores.containsKey(1L));
        assertFalse(scores.containsKey(2L));

        // Scores should be > 0 because similarities exist
        assertTrue(scores.get(3L) > 0);
    }

    @Test
    void returnsEmptyWhenUserHasNoLoans() {
        Map<String, List<String>> loans = Map.of(
                "user1", List.of(),
                "user2", List.of("1", "2")
        );

        Map<Long, Double> scores = service.computeScores("user1", loans);

        assertTrue(scores.isEmpty());
    }
}
