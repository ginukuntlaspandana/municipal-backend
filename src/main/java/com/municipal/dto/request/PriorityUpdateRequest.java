package com.municipal.dto.request;

import com.municipal.entity.ComplaintPriority;
import jakarta.validation.constraints.NotNull;

public class PriorityUpdateRequest {

    @NotNull(message = "Priority is required")
    private ComplaintPriority priority;

    public PriorityUpdateRequest() {
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }
}
