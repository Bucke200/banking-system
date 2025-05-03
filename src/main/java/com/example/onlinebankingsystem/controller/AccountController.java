package com.example.onlinebankingsystem.controller;

import com.example.onlinebankingsystem.dto.AccountDto;
import com.example.onlinebankingsystem.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For method-level security
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map; // For simple request body

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // Endpoint to create a new account for the authenticated user
    // Request body could be simple, e.g., {"initialBalance": 100.00} or empty for zero balance
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResponseEntity<?> createAccount(@RequestBody(required = false) Map<String, BigDecimal> requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get username from authenticated principal

        BigDecimal initialBalance = (requestBody != null && requestBody.containsKey("initialBalance"))
                                    ? requestBody.get("initialBalance")
                                    : BigDecimal.ZERO;

        try {
            AccountDto newAccount = accountService.createAccount(username, initialBalance);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
        } catch (Exception e) {
            // Handle potential errors (e.g., user not found, though unlikely if authenticated)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating account: " + e.getMessage());
        }
    }

    // Endpoint to get all accounts for the authenticated user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            List<AccountDto> accounts = accountService.getAccountsByUsername(username);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving accounts: " + e.getMessage());
        }
    }

    // Endpoint to get a specific account by its number
    // Security: Ensure the authenticated user owns this account
    @GetMapping("/{accountNumber}")
    @PreAuthorize("isAuthenticated()") // Basic check: user must be logged in
    public ResponseEntity<?> getAccountByNumber(@PathVariable String accountNumber) {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         String currentUsername = authentication.getName();

        try {
            AccountDto accountDto = accountService.getAccountByAccountNumber(accountNumber);

            // Authorization check: Does the current user own this account?
            if (!accountDto.getOwnerUsername().equals(currentUsername)) {
                 // Consider if admins should bypass this check - requires role management
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You do not own this account.");
            }

            return ResponseEntity.ok(accountDto);
        } catch (IllegalArgumentException e) { // Specific exception for account not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving account: " + e.getMessage());
        }
    }
}
