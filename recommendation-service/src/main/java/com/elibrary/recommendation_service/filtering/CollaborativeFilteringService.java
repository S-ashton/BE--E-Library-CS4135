package com.elibrary.recommendation_service.filtering;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CollaborativeFilteringService {
    // Computes collaborative filtering scores for books based on similar users.

    public Map<Long, Double> computeScores(String targetUser, Map<String, List<String>> loans)
    {
        List<String> targetLoans = loans.getOrDefault(targetUser, List.of());
        if (targetLoans.isEmpty()) return Map.of();

        Map<Long, Double> scores = new HashMap<>();

        for (var entry : loans.entrySet()) {
            String otherUser = entry.getKey();
            if (otherUser.equals(targetUser)) continue;

            List<String> otherLoans = entry.getValue();
            double similarity = jaccard(targetLoans, otherLoans);

            if (similarity > 0) {
                for (String bookId : otherLoans) {
                    if (!targetLoans.contains(bookId)) {
                        long id = Long.parseLong(bookId);
                        scores.merge(id, similarity, Double::sum);
                    }
                }
            }
        }

        return scores;
    }

    private double jaccard(List<String> a, List<String> b) {
        Set<String> setA = new HashSet<>(a);
        Set<String> setB = new HashSet<>(b);

        int intersection = (int) setA.stream().filter(setB::contains).count();
        int union = setA.size() + setB.size() - intersection;

        return union == 0 ? 0 : (double) intersection / union;
    }
}
