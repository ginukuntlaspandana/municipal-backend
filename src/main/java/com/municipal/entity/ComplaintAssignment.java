package com.municipal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_assignments")
public class ComplaintAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 100)
    private String assignedOfficerName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedByUser;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    public ComplaintAssignment() {
    }

    public ComplaintAssignment(Complaint complaint, Department department, String assignedOfficerName, String notes, User assignedByUser) {
        this.complaint = complaint;
        this.department = department;
        this.assignedOfficerName = assignedOfficerName;
        this.notes = notes;
        this.assignedByUser = assignedByUser;
    }

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getAssignedOfficerName() {
        return assignedOfficerName;
    }

    public void setAssignedOfficerName(String assignedOfficerName) {
        this.assignedOfficerName = assignedOfficerName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getAssignedByUser() {
        return assignedByUser;
    }

    public void setAssignedByUser(User assignedByUser) {
        this.assignedByUser = assignedByUser;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
