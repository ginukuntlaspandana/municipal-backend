package com.municipal.dto.response;

import java.time.LocalDateTime;

public class RemarkDto {
    private Long id;
    private String adminName;
    private String remark;
    private Boolean internalOnly;
    private LocalDateTime createdAt;

    public RemarkDto() {
    }

    public RemarkDto(Long id, String adminName, String remark, Boolean internalOnly, LocalDateTime createdAt) {
        this.id = id;
        this.adminName = adminName;
        this.remark = remark;
        this.internalOnly = internalOnly;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
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
