package com.municipal.service;

import com.municipal.dto.request.LoginRequest;
import com.municipal.dto.request.PasswordChangeRequest;
import com.municipal.dto.request.ProfileUpdateRequest;
import com.municipal.dto.request.RegisterRequest;
import com.municipal.dto.response.AuthResponse;
import com.municipal.entity.Role;
import com.municipal.entity.User;
import com.municipal.exception.BadRequestException;
import com.municipal.exception.ResourceNotFoundException;
import com.municipal.repository.UserRepository;
import com.municipal.security.JwtUtils;
import com.municipal.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email address is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone().trim(),
                request.getAddress() != null ? request.getAddress().trim() : "",
                Role.ROLE_CITIZEN
        );

        User savedUser = userRepository.save(user);

        // Auto authenticate on register
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(savedUser.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getPhone(),
                savedUser.getAddress()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getPhone(),
                user.getAddress()
        );
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BadRequestException("User is not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));
    }

    @Transactional
    public User updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentAuthenticatedUser();
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : "");
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new BadRequestException("New password cannot be identical to current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
