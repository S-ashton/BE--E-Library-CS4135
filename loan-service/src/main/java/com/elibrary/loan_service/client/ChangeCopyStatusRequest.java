package com.elibrary.loan_service.client;

public class ChangeCopyStatusRequest {

    private Long copyId;
    private String status;

    public ChangeCopyStatusRequest() {
    }

    public ChangeCopyStatusRequest(Long copyId, String status) {
        this.copyId = copyId;
        this.status = status;
    }

    public Long getCopyId() {
        return copyId;
    }

    public String getStatus() {
        return status;
    }
}