package com.elibrary.book_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Retrieve a single copy's details from the database")
public class CopyResponse {

    @Schema(description = "Unique copy id", example = 1)
    private Long id;

    @Schema(description = "ID number of the title, as assigned in the Books table", example = "1")
    @NotBlank(message = "A title ID must be provided")
    private Long book_id;

    @Schema(description = "Current status of the copy", example = "AVAILABLE")
    @NotBlank(message = "The copy must have a status at all times")
    private Status status;

    public CopyResponse() {}

    public CopyResponse(Long id, Long book_id, Status status) {
        this.id = id;
        this.book_id = book_id;
        this.status = status;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public Long getBookId() {
        return book_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
