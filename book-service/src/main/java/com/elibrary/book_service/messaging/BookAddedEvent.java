package com.elibrary.book_service.messaging;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import com.elibrary.book_service.model.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Retrieve a single title's details from the database")
public class BookAddedEvent {

    private Long id;

    private String title;

    private String author;

    private String description;

    private int yearPublished;

    private Genre genre;

    private byte[] coverImage;

    private Languages language;

    private int copiesAvailable;

    public BookAddedEvent() {}

    public BookAddedEvent(Long id, String title, String author, String description, int yearPublished, Genre genre, byte[] coverImage, Languages language, int copiesAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.coverImage = coverImage;
        this.language = language;
        this.copiesAvailable = copiesAvailable;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
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

    public int getYearPublished(){
        return yearPublished;
    }

    public void setYearPublished(int yearPublished){
        this.yearPublished = yearPublished;        
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

    public int getCopiesAvailable(){
        return copiesAvailable;
    }
}

