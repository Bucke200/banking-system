package com.example.onlinebankingsystem.service;

import com.example.onlinebankingsystem.dto.TransactionDto;
import com.example.onlinebankingsystem.dto.TransferRequestDto;
import com.example.onlinebankingsystem.model.Account;
import com.example.onlinebankingsystem.model.Transaction;
import com.example.onlinebankingsystem.repository.AccountRepository;
import com.example.onlinebankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Crucial for atomicity

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional // Ensures the entire transfer operation is atomic
    public TransactionDto transferFunds(TransferRequestDto transferRequest, String currentUsername) {
        // 1. Validate accounts
        Account fromAccount = accountRepository.findByAccountNumber(transferRequest.getFromAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + transferRequest.getFromAccountNumber()));
        Account toAccount = accountRepository.findByAccountNumber(transferRequest.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + transferRequest.getToAccountNumber()));

        // 2. Check ownership of the source account
        if (!fromAccount.getOwner().getUsername().equals(currentUsername)) {
            throw new SecurityException("User does not own the source account.");
        }

        // 3. Check for sufficient funds
        if (fromAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account.");
        }

        // 4. Perform the transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));

        // 5. Save updated accounts
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 6. Create and save the transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(transferRequest.getAmount());
        transaction.setType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(transferRequest.getDescription());
        // timestamp is set by @PrePersist

        Transaction savedTransaction = transactionRepository.save(transaction);

        return convertToDto(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionHistory(String accountNumber, String currentUsername) {
        // 1. Find the account
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        // 2. Check ownership
        if (!account.getOwner().getUsername().equals(currentUsername)) {
            throw new SecurityException("User does not own this account.");
        }

        // 3. Retrieve transactions
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTimestampDesc(account.getId());

        // 4. Convert to DTOs
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- Helper Method ---

    private TransactionDto convertToDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null, // Handle potential null fromAccount (e.g., deposits)
                transaction.getToAccount().getAccountNumber(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getType(),
                transaction.getDescription()
        );
    }
}
