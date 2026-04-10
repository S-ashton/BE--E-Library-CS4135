package com.elibrary.book_service.model;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.elibrary.book_service.dto.TitleResponseDTO;


import jakarta.persistence.*;

@Entity
@Table(name = "books")
@Document(indexName = "books", createIndex = true)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Field(type = FieldType.Text)
    private String title;

    @Column(nullable = false)
    @Field(type = FieldType.Text)
    private String author;

    @Column(nullable = false)
    @Field(type = FieldType.Text)
    private String description;

    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private int yearPublished;

    @Enumerated(EnumType.STRING)
    @Field(type = FieldType.Keyword)
    @Column(nullable = false)
    private Genre genre;

    @Lob
    @Column(name = "cover_image", columnDefinition = "BYTEA")
    private byte[] coverImage;

    @Enumerated(EnumType.STRING)
    @Field(type = FieldType.Keyword)
    @Column(nullable = false)
    private Languages language;

    @Column(nullable = false)
    private int copiesAvailable;




    protected Book() {}

    public Book(String title, String author, String description, int yearPublished, Genre genre, byte[] coverImage, Languages language) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.coverImage = coverImage;
        this.language = language;
        this.copiesAvailable = 1;
    }

    public Book(String title, String author, String description, int yearPublished, Genre genre, byte[] coverImage, Languages language, int copiesAvailable) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.coverImage = coverImage;
        this.language = language;
        this.copiesAvailable = copiesAvailable;
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

    public void setCopiesAvailable(int numCopies){
        this.copiesAvailable = numCopies;
    }

    public TitleResponseDTO toDto (){
        return new TitleResponseDTO(this.id, this.title, this.author, this.description,
                                    this.yearPublished, this.genre, this.coverImage, 
                                    this.language, this.copiesAvailable);
    }
}
