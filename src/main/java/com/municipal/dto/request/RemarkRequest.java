package com.municipal.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RemarkRequest {

    @NotBlank(message = "Remark cannot be blank")
    private String remark;

    private Boolean internalOnly = false;

    public RemarkRequest() {
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
}
