package com.municipal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Phone cannot be empty")
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "Please enter a valid phone number")
    private String phone;

    private String address;

    public ProfileUpdateRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
