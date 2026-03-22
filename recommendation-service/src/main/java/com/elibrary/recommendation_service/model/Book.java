package com.elibrary.recommendation_service.model;

public class Book {
    private long id;
    private String title;
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