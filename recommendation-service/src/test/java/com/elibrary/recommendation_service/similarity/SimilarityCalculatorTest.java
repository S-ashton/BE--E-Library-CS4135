package com.elibrary.recommendation_service.similarity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityCalculatorTest {

    SimilarityCalculator calc = new SimilarityCalculator();

    @Test
    void testIdenticalVectors() {
        float[] a = {1f, 2f, 3f};
        float[] b = {1f, 2f, 3f};

        double sim = calc.cosineSimilarity(a, b);

        assertEquals(1.0, sim, 1e-6);
    }

    @Test
    void testOppositeVectors() {
        float[] a = {1f, 0f};
        float[] b = {-1f, 0f};

        double sim = calc.cosineSimilarity(a, b);

        assertEquals(-1.0, sim, 1e-6);
    }

    @Test
    void testOrthogonalVectors() {
        float[] a = {1f, 0f};
        float[] b = {0f, 1f};

        double sim = calc.cosineSimilarity(a, b);

        assertEquals(0.0, sim, 1e-6);
    }

    @Test
    void testDifferentLengthsThrows() {
        float[] a = {1f, 2f};
        float[] b = {1f};

        assertThrows(IllegalArgumentException.class, () -> calc.cosineSimilarity(a, b));
    }

    @Test
    void testZeroVectorReturnsZero() {
        float[] a = {0f, 0f, 0f};
        float[] b = {1f, 2f, 3f};

        double sim = calc.cosineSimilarity(a, b);

        assertEquals(0.0, sim, 1e-6);
    }
}
