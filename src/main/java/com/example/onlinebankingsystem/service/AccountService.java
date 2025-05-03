package com.example.onlinebankingsystem.service;

import com.example.onlinebankingsystem.dto.AccountDto;
import com.example.onlinebankingsystem.model.Account;
import com.example.onlinebankingsystem.model.User;
import com.example.onlinebankingsystem.repository.AccountRepository;
import com.example.onlinebankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID; // For generating account numbers
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AccountDto createAccount(String username, BigDecimal initialBalance) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Account newAccount = new Account();
        newAccount.setOwner(owner);
        newAccount.setAccountNumber(generateUniqueAccountNumber());
        newAccount.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        // createdAt and updatedAt are handled by @PrePersist

        Account savedAccount = accountRepository.save(newAccount);
        return convertToDto(savedAccount);
    }

    @Transactional(readOnly = true) // Read-only transaction for retrieval
    public List<AccountDto> getAccountsByUsername(String username) {
        // Ensure user exists (optional, depends on whether you want to return empty list or error)
         if (!userRepository.existsByUsername(username)) {
             throw new UsernameNotFoundException("User not found: " + username);
         }

        List<Account> accounts = accountRepository.findByOwnerUsername(username);
        return accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
        return convertToDto(account);
    }

    // --- Helper Methods ---

    private String generateUniqueAccountNumber() {
        // Simple UUID-based generation. Could be more sophisticated (e.g., sequential with check digit).
        String accountNumber;
        do {
            accountNumber = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12); // Example: 12 chars
        } while (accountRepository.existsByAccountNumber(accountNumber)); // Ensure uniqueness
        return accountNumber;
    }

    private AccountDto convertToDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getOwner().getUsername(), // Get username from the owner User object
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
