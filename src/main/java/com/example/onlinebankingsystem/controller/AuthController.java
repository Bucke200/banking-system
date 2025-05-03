package com.example.onlinebankingsystem.controller;

import com.example.onlinebankingsystem.dto.AuthenticationRequestDto;
import com.example.onlinebankingsystem.dto.AuthenticationResponseDto;
import com.example.onlinebankingsystem.dto.UserRegistrationDto;
import com.example.onlinebankingsystem.service.AuthService;
import jakarta.validation.Valid; // Import Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Base path for authentication endpoints
public class AuthController {

    @Autowired
    private AuthService authService;

    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            authService.registerUser(registrationDto);
            // Consider returning the created user details (without password) or just a success message
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (IllegalArgumentException e) {
            // Handle specific exceptions like username/email already exists
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Generic error handler
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration.");
        }
    }

    // Endpoint for user login (authentication)
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthenticationRequestDto authenticationRequest) {
        try {
            AuthenticationResponseDto response = authService.authenticateUser(authenticationRequest);
            return ResponseEntity.ok(response); // Return JWT token on successful authentication
        } catch (Exception e) {
            // Handle authentication failures (e.g., BadCredentialsException)
            // Consider returning a more specific status code like UNAUTHORIZED
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }
}
