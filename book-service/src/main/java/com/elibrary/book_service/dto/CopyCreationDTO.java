package com.elibrary.book_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import com.elibrary.book_service.model.Status;

@Schema(description = "Add a new copy of a title to the library")
public class CopyCreationDTO {

    @Schema(description = "ID number of the title, as assigned in the Books table", example = "1")
    @NotBlank(message = "A title ID must be provided")
    private Long book_id;

    @Schema(description = "Current status of the copy", example = "AVAILABLE")
    @NotBlank(message = "The copy must have a status at all times")
    private Status status;

    public CopyCreationDTO() {}

    public CopyCreationDTO(Long book_id, Status status) {
        this.book_id = book_id;
        this.status = status;
    }

    public Long getBookId() {
        return book_id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
