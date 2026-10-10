package com.municipal.dto.request;

import com.municipal.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private ComplaintStatus status;

    private String remarks;

    private String rejectionReason;

    private String resolutionDetails;

    public StatusUpdateRequest() {
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getResolutionDetails() {
        return resolutionDetails;
    }

    public void setResolutionDetails(String resolutionDetails) {
        this.resolutionDetails = resolutionDetails;
    }
}
