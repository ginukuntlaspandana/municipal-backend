package com.municipal.controller;

import com.municipal.dto.request.PasswordChangeRequest;
import com.municipal.dto.request.ProfileUpdateRequest;
import com.municipal.dto.response.ApiResponse;
import com.municipal.entity.User;
import com.municipal.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile() {
        User user = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        User updatedUser = authService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
