package com.elibrary.book_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Change the status of a copy")
public class ChangeCopyStatusRequest {
    
    @Schema(description = "New current status of the copy", example = "AVAILABLE")
    @NotBlank(message = "The copy must have a status at all times")
    private Status status;

    public AddNewCopyRequest() {}

    public AddNewCopyRequest(Status status) {
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