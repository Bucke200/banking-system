package com.example.onlinebankingsystem.dto;

import com.example.onlinebankingsystem.model.Transaction; // Import Transaction model
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private String fromAccountNumber; // Use account number instead of full Account object
    private String toAccountNumber;   // Use account number instead of full Account object
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private Transaction.TransactionType type; // Use the enum from Transaction model
    private String description;
}
