package com.elibrary.recommendation_service.similarity;

import org.springframework.stereotype.Component;

@Component
public class SimilarityCalculator {

    public double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double dot = 0.0;
        double magA = 0.0;
        double magB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }

        if (magA == 0 || magB == 0) {
            return 0.0;
        }

        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }
}
