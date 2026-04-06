package com.elibrary.book-service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Add a new copy of a title to the library")
public class AddNewCopyRequest {

    @Schema(description = "ID number of the title, as assigned in the Books table", example = "1")
    @NotBlank(message = "A title ID must be provided")
    private Long book_id;

    @Schema(description = "Current status of the copy", example = "AVAILABLE")
    @NotBlank(message = "The copy must have a status at all times")
    private Status status;

    public AddNewCopyRequest() {}

    public AddNewCopyRequest(Long book_id, Status status) {
        this.book_id = book_id;
        this.status = status;
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
