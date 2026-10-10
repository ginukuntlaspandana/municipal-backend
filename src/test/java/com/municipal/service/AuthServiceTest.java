package com.municipal.service;

import com.municipal.dto.request.LoginRequest;
import com.municipal.dto.request.RegisterRequest;
import com.municipal.dto.response.AuthResponse;
import com.municipal.entity.Role;
import com.municipal.entity.User;
import com.municipal.exception.BadRequestException;
import com.municipal.repository.UserRepository;
import com.municipal.security.JwtUtils;
import com.municipal.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("John Citizen", "john@example.com", "encodedPassword", "9876543210", "123 Main St", Role.ROLE_CITIZEN);
        sampleUser.setId(10L);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("John Citizen");
        req.setEmail("john@example.com");
        req.setPassword("Password@123");
        req.setPhone("9876543210");
        req.setAddress("123 Main St");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("mock.jwt.token");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("john@example.com", resp.getEmail());
        assertEquals("mock.jwt.token", resp.getToken());
        assertEquals("ROLE_CITIZEN", resp.getRole());
    }

    @Test
    void testRegisterDuplicateEmailThrowsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("john@example.com");
        req.setPassword("Password@123");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("john@example.com", "Password@123");

        UserDetailsImpl userDetails = UserDetailsImpl.build(sampleUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("mock.jwt.token");
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("mock.jwt.token", resp.getToken());
        assertEquals("john@example.com", resp.getEmail());
    }
}
