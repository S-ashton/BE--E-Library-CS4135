package com.elibrary.recommendation_service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A recommended book with its relevance score")
public class Recommendation {
    @Schema(description = "ID of the recommended book", example = "3")
    private long bookId;
    @Schema(description = "Relevance score between 0 and 1", example = "0.87")
    private double score;

    public Recommendation() {}

    public Recommendation(Long bookId, double score)
    {
        this.bookId = bookId;
        this.score = score;
    }

    public Long getBookId() { return bookId; }
    public double getScore() { return score; }

    public void setBookId(Long bookId) { this.bookId = bookId; }
    public void setScore(double score) { this.score = score; }
}
