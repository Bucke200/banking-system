package com.example.onlinebankingsystem.service;

import com.example.onlinebankingsystem.dto.AuthenticationRequestDto;
import com.example.onlinebankingsystem.dto.AuthenticationResponseDto;
import com.example.onlinebankingsystem.dto.UserRegistrationDto;
import com.example.onlinebankingsystem.model.User;
import com.example.onlinebankingsystem.repository.UserRepository;
import com.example.onlinebankingsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Correct import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.Set; // Import Set

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService; // Use UserDetailsService interface

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional // Ensure registration is atomic
    public User registerUser(UserRegistrationDto registrationDto) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        // Encode the password before saving
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        // Assign default role (e.g., "ROLE_USER")
        newUser.setRoles(Set.of("ROLE_USER")); // Use Set.of for immutable set

        return userRepository.save(newUser);
    }

    public AuthenticationResponseDto authenticateUser(AuthenticationRequestDto authRequest) {
        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect username or password", e);
        }

        // If authentication is successful, load UserDetails
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

        // Generate JWT token
        final String jwt = jwtUtil.generateToken(userDetails);

        // Return the token in the response DTO
        return new AuthenticationResponseDto(jwt);
    }
}
