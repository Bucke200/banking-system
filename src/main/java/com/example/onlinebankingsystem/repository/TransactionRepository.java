package com.example.onlinebankingsystem.repository;

import com.example.onlinebankingsystem.model.Account;
import com.example.onlinebankingsystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions where the given account is either the sender or the receiver
    // Ordered by timestamp descending (most recent first)
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountOrderByTimestampDesc(@Param("account") Account account);

    // Alternative using method naming convention (might be less efficient depending on JPA implementation)
    // List<Transaction> findByFromAccountOrToAccountOrderByTimestampDesc(Account fromAccount, Account toAccount);

    // Find transactions for a specific account by its ID
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountIdOrderByTimestampDesc(@Param("accountId") Long accountId);

}
