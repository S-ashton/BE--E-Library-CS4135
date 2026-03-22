package com.elibrary.recommendation_service.model;

public class Recommendation {
    private long bookId;
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
