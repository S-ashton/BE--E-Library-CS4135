package com.elibrary.book_service.model;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.elibrary.book_service.dto.TitleResponseDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
@Document(indexName = "books", createIndex = true)
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @Column(name = "cover_image_url")
    @Field(type = FieldType.Text)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Field(type = FieldType.Keyword)
    @Column(nullable = false)
    private Languages language;

    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private int copiesAvailable;

    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private int totalCopies;


    protected Book() {}

    public Book(String title, String author, String description, int yearPublished, Genre genre, String coverImageUrl, Languages language) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.coverImageUrl = coverImageUrl;
        this.language = language;
        this.copiesAvailable = 1;
        this.totalCopies = 1;
    }

    public Book(String title, String author, String description, int yearPublished, Genre genre, String coverImageUrl, Languages language, int copiesAvailable) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.coverImageUrl = coverImageUrl;
        this.language = language;
        this.copiesAvailable = copiesAvailable;
        this.totalCopies = copiesAvailable;
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

    public String getCoverImageUrl(){
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Languages getLanguage() {
        return language;
    }

    public void setLanguage(Languages language) {
        this.language = language;
    }

    public int getCopiesAvailable() {
        return copiesAvailable;
    }

    public void setCopiesAvailable(int numCopies) {
        this.copiesAvailable = numCopies;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public TitleResponseDTO toDto() {
        return new TitleResponseDTO(
                this.id,
                this.title,
                this.author,
                this.description,
                this.yearPublished,
                this.genre,
                this.coverImageUrl,
                this.language,
                this.copiesAvailable,
                this.totalCopies
        );
    }
}