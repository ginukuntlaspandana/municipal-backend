package com.municipal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "administrative_remarks")
public class AdministrativeRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String remark;

    @Column(nullable = false)
    private Boolean internalOnly = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AdministrativeRemark() {
    }

    public AdministrativeRemark(Complaint complaint, User admin, String remark, Boolean internalOnly) {
        this.complaint = complaint;
        this.admin = admin;
        this.remark = remark;
        this.internalOnly = internalOnly != null ? internalOnly : false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.internalOnly == null) {
            this.internalOnly = false;
        }
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

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getInternalOnly() {
        return internalOnly;
    }

    public void setInternalOnly(Boolean internalOnly) {
        this.internalOnly = internalOnly;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
