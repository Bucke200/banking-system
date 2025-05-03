package com.example.onlinebankingsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String ownerUsername; // Include owner's username instead of the full User object
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
