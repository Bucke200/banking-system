package com.example.onlinebankingsystem.controller;

import com.example.onlinebankingsystem.dto.TransactionDto;
import com.example.onlinebankingsystem.dto.TransferRequestDto;
import com.example.onlinebankingsystem.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Endpoint to perform a fund transfer
    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()") // User must be logged in
    public ResponseEntity<?> transferFunds(@Valid @RequestBody TransferRequestDto transferRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        try {
            TransactionDto transactionDto = transactionService.transferFunds(transferRequest, currentUsername);
            return ResponseEntity.ok(transactionDto);
        } catch (IllegalArgumentException e) {
            // Handle specific errors like account not found, insufficient funds
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            // Handle authorization errors (user doesn't own source account)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            // Generic error handler
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the transfer: " + e.getMessage());
        }
    }

    // Endpoint to get transaction history for a specific account
    @GetMapping("/{accountNumber}")
    @PreAuthorize("isAuthenticated()") // User must be logged in
    public ResponseEntity<?> getTransactionHistory(@PathVariable String accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        try {
            // The service layer handles the ownership check
            List<TransactionDto> history = transactionService.getTransactionHistory(accountNumber, currentUsername);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            // Account not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            // User does not own the account
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving transaction history: " + e.getMessage());
        }
    }
}
