package com.elibrary.book_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.elibrary.book_service.model.*;

@Schema(description = "Add a new title to the library")
public class TitleRequestDTO {

    @Schema(description = "Book title", example = "Don't Let The Pigeon Drive The Bus")
    @NotBlank(message = "A title is required")
    private String title;

    @Schema(description = "Author's name", example = "Mo Willems")
    @NotBlank(message = "Author name(s) must be listed")
    private String author;

    @Schema(description = "Book blurb or description of contents", example = "When a bus driver takes a break in this hilarious Caldecott Honor-winning picture book, he gives the reader just one instruction: 'Don't let the pigeon drive the bus!' But, boy, that pigeon tries every trick in the book to get in that driving seat: he whines, wheedles, fibs and flatters. Will you let him drive? Told entirely in speech bubbles, this brilliantly original and funny picture book demands audience participation!")
    @NotBlank(message = "Blurb or placeholder text must be provided")
    private String description;

    @Schema(description = "The year the book was published", example = "2005")
    @NotNull(message = "Year of publication must be provided")
    private Integer yearPublished;

    @Schema(description = "The most prevalent genre of the book", example = "CHILDREN")
    @NotNull(message = "A genre from the list must be provided")
    private Genre genre;

    @Schema(description = "The cover image for the book")
    private byte[] coverImage;

    @Schema(description = "The primary language in which the book is written", example = "ENGLISH")
    @NotNull(message = "A language from the list must be provided")
    private Languages language;

    public TitleRequestDTO() {}

    public TitleRequestDTO(String title, String author, String description, int yearPublished, Genre genre, byte[] coverImage, Languages language) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.coverImage = coverImage;
        this.language = language;
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

    public Integer getYearPublished(){
        return yearPublished;
    }

    public void setYearPublished(Integer yearPublished){
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
}
