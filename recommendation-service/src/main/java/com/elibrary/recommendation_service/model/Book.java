package com.elibrary.recommendation_service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Book metadata used for recommendation scoring")
public class Book {
    @Schema(description = "Book ID", example = "1")
    private long id;
    @Schema(description = "Book title", example = "Deep Learning with BERT")
    private String title;
    @Schema(description = "Book description used for embeddings")
    private String description;

    public Book() {}

    public Book(long _id, String _title, String _description)
    {
        id = _id;
        title = _title;
        description = _description;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}