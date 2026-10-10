package com.municipal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_status_history")
public class ComplaintStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ComplaintStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus newStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changed_by_user_id")
    private User changedByUser;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public ComplaintStatusHistory() {
    }

    public ComplaintStatusHistory(Complaint complaint, ComplaintStatus previousStatus, ComplaintStatus newStatus, User changedByUser, String remarks) {
        this.complaint = complaint;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedByUser = changedByUser;
        this.remarks = remarks;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
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

    public User getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(User changedByUser) {
        this.changedByUser = changedByUser;
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
