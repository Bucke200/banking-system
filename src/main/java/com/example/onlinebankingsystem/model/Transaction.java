package com.example.onlinebankingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id") // Nullable for deposits/initial balance? Or handle differently.
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Store enum names (e.g., "TRANSFER", "DEPOSIT")
    private TransactionType type;

    private String description; // Optional description

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Enum for different transaction types
    public enum TransactionType {
        TRANSFER,
        DEPOSIT,
        WITHDRAWAL // Add more types as needed
    }
}
