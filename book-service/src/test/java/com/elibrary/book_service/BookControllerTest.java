package com.elibrary.book_service;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.elibrary.book_service.controller.BookController;
import com.elibrary.book_service.dto.CopyCreationDTO;
import com.elibrary.book_service.dto.CopyResponseDTO;
import com.elibrary.book_service.dto.TitleRequestDTO;
import com.elibrary.book_service.dto.TitleResponseDTO;
import com.elibrary.book_service.model.Book;
import com.elibrary.book_service.model.Genre;
import com.elibrary.book_service.model.Languages;
import com.elibrary.book_service.model.Status;
import com.elibrary.book_service.repository.elasticsearch.ElasticsearchRepo;
import com.elibrary.book_service.repository.jpa.*;
import com.elibrary.book_service.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookControllerTest {

    private static final String AUTH_USER_HEADER = "X-Authenticated-User-Id";
    private static final String AUTHENTICATED_USER_ID_HEADER = "X-Authenticated-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private TitleRepository titleRepository;

    @MockBean
    private ElasticsearchRepo elasticsearchRepo;

    @Autowired
    private ObjectMapper objectMapper;

    MockMultipartFile title =
    new MockMultipartFile("title", "", "text/plain", "Title".getBytes());

MockMultipartFile author =
    new MockMultipartFile("author", "", "text/plain", "Author".getBytes());

MockMultipartFile description =
    new MockMultipartFile("description", "", "text/plain", "This is a book".getBytes());

MockMultipartFile yearPublished =
    new MockMultipartFile("yearPublished", "", "text/plain", "2026".getBytes());

MockMultipartFile genre =
    new MockMultipartFile("genre", "", "text/plain", "CHILDREN".getBytes());

MockMultipartFile language =
    new MockMultipartFile("language", "", "text/plain", "ENGLISH".getBytes());


MockMultipartFile coverImage =
    new MockMultipartFile("coverImage", "cover.jpg", "image/jpeg", "fake".getBytes());

    @Test
    @DisplayName("POST /api/books/addTitle creates a new title")
    void addBook_success() throws Exception {
        TitleResponseDTO response = new TitleResponseDTO(
            1L, "Title", "Author", "This is a book", 2026, Genre.CHILDREN, "mockUrl", Languages.ENGLISH, 1
        );

        
        when(bookService.addTitle(
                any(TitleRequestDTO.class)
        )).thenReturn(response);

        mockMvc.perform(multipart("/api/books/addTitle")
                        .file(coverImage)
                        .param("title", "Title")
                        .param("author", "Author")
                        .param("description", "This is a book")
                        .param("yearPublished", "2026")
                        .param("genre", "CHILDREN")
                        .param("language", "ENGLISH")
                        .header(AUTH_USER_HEADER, "1")
                        .header(AUTHENTICATED_USER_ID_HEADER, 1L)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })).andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.author").value("Author"))
                .andExpect(jsonPath("$.description").value("This is a book"))
                .andExpect(jsonPath("$.yearPublished").value(2026))
                .andExpect(jsonPath("$.genre").value("CHILDREN"))
                .andExpect(jsonPath("$.coverImageUrl").exists())
                .andExpect(jsonPath("$.language").value("ENGLISH"))
                .andExpect(jsonPath("$.copiesAvailable").value(1));

        verify(bookService).addTitle(
                any(TitleRequestDTO.class)
        );
    }

    @Test
    @DisplayName("POST /api/books/addCopy creates a new copy")
    void addCopy_success() throws Exception {
        CopyResponseDTO response = new CopyResponseDTO(
            1L, 1L, Status.AVAILABLE
        );

        
        when(bookService.addCopy(
                any(Long.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/books/addCopy")
                        .param("titleId", "1")
                        .header(AUTH_USER_HEADER, "1")
                        .header(AUTHENTICATED_USER_ID_HEADER, 1L)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookId").value(1L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        verify(bookService).addCopy(
                any(Long.class)
        );
    }

    @Test
    @DisplayName("POST /api/books/addCopy creates a new copy")
    void changeCopyStatus_success() throws Exception {
        CopyResponseDTO response = new CopyResponseDTO(
            1L, 1L, Status.ON_LOAN
        );

        
        when(bookService.changeStatus(
            any(Long.class),    
            any(Status.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/books/changeStatus")
                        .param("copyId", "1")
                        .param("status", "ON_LOAN")
                        .header(AUTH_USER_HEADER, "1")
                        .header(AUTHENTICATED_USER_ID_HEADER, 1L)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookId").value(1L))
                .andExpect(jsonPath("$.status").value("ON_LOAN"));

        verify(bookService).changeStatus(
                any(Long.class),
                any(Status.class)
        );
    }

    @Test
    @DisplayName("POST /api/books/{id} retrieves a copy")
    void retrieveTitle_success() throws Exception {
        
        createBook("Book Title", "Jonah J Jameson", "This is a book", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH);
        
        TitleResponseDTO response = new TitleResponseDTO(
            1L, "Book Title", "Jonah J Jameson", "This is a book", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH, 1
        );

        
        when(bookService.getTitle(
            any(Long.class)
        )).thenReturn(response);

        mockMvc.perform(get("/api/books/1")
                        .header(AUTH_USER_HEADER, "1")
                        .header(AUTHENTICATED_USER_ID_HEADER, 1L)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Book Title"))
                .andExpect(jsonPath("$.author").value("Jonah J Jameson"))
                .andExpect(jsonPath("$.description").value("This is a book"))
                .andExpect(jsonPath("$.yearPublished").value(2026))
                .andExpect(jsonPath("$.genre").value("NONFICTION"))
                .andExpect(jsonPath("$.coverImageUrl").value("URL"))
                .andExpect(jsonPath("$.language").value("ENGLISH"))
                .andExpect(jsonPath("$.copiesAvailable").value(1));

        verify(bookService).getTitle(
                any(Long.class)
        );
    }
    
    // @Test
    // @DisplayName("GET /api/books/search returns search results")
    // void keywordSearch_success() throws Exception {
        
    //     createBook("Book Title", "Jonah J Jameson", "This is a book", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH);
    //     createBook("Book Title2", "Josh Dun", "This is another book", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH);
    //     createBook("Cool Novel", "Maria Kelly", "This is a novel", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH);

    //     List<TitleResponseDTO> response = new ArrayList<>();
    //     response.add(new TitleResponseDTO(1L, "Cool Novel", "Maria Kelly", "This is a novel", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH));
                
    //     when(bookService.search(
    //         any(String.class),
    //         any(Genre.class),
    //         any(Integer.class),
    //         any(Languages.class)
    //     )).thenReturn(response);

    //     System.out.println(bookService.getClass());

    //     mockMvc.perform(get("/api/books/search")
    //                     .param("keyword", "Novel")
    //                     .header(AUTH_USER_HEADER, "1")
    //                     .header(AUTHENTICATED_USER_ID_HEADER, 1L))
    //                     .andDo(print())
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$[0].id").value(1L))
    //             .andExpect(jsonPath("$[0].title").value("Cool Novel"))
    //             .andExpect(jsonPath("$[0].author").value("Maria Kelly"))
    //             .andExpect(jsonPath("$.description").value("This is a novel"))
    //             .andExpect(jsonPath("$.yearPublished").value(2026))
    //             .andExpect(jsonPath("$.genre").value("NONFICTION"))
    //             .andExpect(jsonPath("$.coverImageUrl").value("URL"))
    //             .andExpect(jsonPath("$.language").value("ENGLISH"))
    //             .andExpect(jsonPath("$.copiesAvailable").value(1));

    //     verify(bookService).search(
    //         any(String.class),
    //         any(Genre.class),
    //         any(Integer.class),
    //         any(Languages.class)
    //     );
    // }

    // @Test
    // @DisplayName("POST /api/books/{id} retrieves a copy")
    // void retrieveTitles_success() throws Exception {
        
    //     createBook("Book Title", "Jonah J Jameson", "This is a book", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH);
        
    //     TitleResponseDTO response = new TitleResponseDTO(
    //         1L, "Book Title", "Jonah J Jameson", "This is a book", 2026, Genre.NONFICTION, "URL", Languages.ENGLISH
    //     );

        
    //     when(bookService.getTitle(
    //         any(Long.class)
    //     )).thenReturn(response);

    //     mockMvc.perform(get("/api/books/1")
    //                     .header(AUTH_USER_HEADER, "1")
    //                     .header(AUTHENTICATED_USER_ID_HEADER, 1L)
    //                     .contentType(MediaType.MULTIPART_FORM_DATA))
    //                     .andDo(print())
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.id").value(1L))
    //             .andExpect(jsonPath("$.title").value("Book Title"))
    //             .andExpect(jsonPath("$.author").value("Jonah J Jameson"))
    //             .andExpect(jsonPath("$.description").value("This is a book"))
    //             .andExpect(jsonPath("$.yearPublished").value(2026))
    //             .andExpect(jsonPath("$.genre").value("NONFICTION"))
    //             .andExpect(jsonPath("$.coverImageUrl").value("URL"))
    //             .andExpect(jsonPath("$.language").value("ENGLISH"))
    //             .andExpect(jsonPath("$.copiesAvailable").value(1));

    //     verify(bookService).getTitle(
    //             any(Long.class)
    //     );
    // }

    private Book createBook(String title, String author, String description, int yearPublished, Genre genre, String coverImageUrl, Languages language) {
        return titleRepository.save(new Book(title, author, description, yearPublished, genre, coverImageUrl, language));
    }


}
