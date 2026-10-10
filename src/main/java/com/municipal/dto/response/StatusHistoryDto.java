package com.municipal.dto.response;

import com.municipal.entity.ComplaintStatus;
import java.time.LocalDateTime;

public class StatusHistoryDto {
    private Long id;
    private ComplaintStatus previousStatus;
    private ComplaintStatus newStatus;
    private String changedByName;
    private String remarks;
    private LocalDateTime timestamp;

    public StatusHistoryDto() {
    }

    public StatusHistoryDto(Long id, ComplaintStatus previousStatus, ComplaintStatus newStatus, String changedByName, String remarks, LocalDateTime timestamp) {
        this.id = id;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedByName = changedByName;
        this.remarks = remarks;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComplaintStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ComplaintStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ComplaintStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
