package com.elibrary.book_service.model;

import jakarta.persistence.*;

//TODO: Implement tags/keywords and stored procedure for copiesAvailable

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long book_id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate datePublished;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    // tags/keywords

    @Lob
    @Column(name = "cover_image", columnDefinition = "BYTEA")
    private byte[] coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Languages language;

    // copiesAvailable (stored procedure)




    protected Book() {}

    public Book(String title, String author, String description, LocalDate datePublished, Genre genre, byte[] coverImage, Languages language) {
        this(title, author, description, datePublished, genre, coverImage, language);
    }

    public Long getId() {
        return id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getAuthor(){
        return author;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public LocalDate getDatePublished(){
        return datePublished;
    }

    public void setDatePublished(LocalDate datePublished){
        this.datePublished = datePublished;        
    }

    public Genre getGenre(){
        return genre;
    }

    public void setGenre(Genre genre){
        this.genre = genre;
    }

    public byte[] getCoverImage(){
        return coverImage;
    }

    public void setCoverImage(byte[] coverImage){
        this.coverImage = coverImage;
    }

    public Languages getLanguage(){
        return language;
    }

    public void setLanguage(Languages language){
        this.language = language;
    }
}
