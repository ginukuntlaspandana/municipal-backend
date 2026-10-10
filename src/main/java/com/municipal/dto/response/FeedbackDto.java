package com.municipal.dto.response;

import java.time.LocalDateTime;

public class FeedbackDto {
    private Long id;
    private Long complaintId;
    private String complaintNumber;
    private String complaintTitle;
    private String citizenName;
    private Integer rating;
    private String comments;
    private LocalDateTime createdAt;

    public FeedbackDto() {
    }

    public FeedbackDto(Long id, Long complaintId, String complaintNumber, String complaintTitle, String citizenName, Integer rating, String comments, LocalDateTime createdAt) {
        this.id = id;
        this.complaintId = complaintId;
        this.complaintNumber = complaintNumber;
        this.complaintTitle = complaintTitle;
        this.citizenName = citizenName;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public String getComplaintTitle() {
        return complaintTitle;
    }

    public void setComplaintTitle(String complaintTitle) {
        this.complaintTitle = complaintTitle;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
