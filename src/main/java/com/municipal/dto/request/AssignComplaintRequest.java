package com.municipal.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignComplaintRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private String assignedOfficerName;

    private String notes;

    public AssignComplaintRequest() {
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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
}
