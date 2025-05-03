package com.example.onlinebankingsystem.repository;

import com.example.onlinebankingsystem.model.Account;
import com.example.onlinebankingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Find an account by its unique account number
    Optional<Account> findByAccountNumber(String accountNumber);

    // Find all accounts associated with a specific owner (User)
    List<Account> findByOwner(User owner);

    // Find all accounts associated with a specific owner's username
    List<Account> findByOwnerUsername(String username);

    // Check if an account number already exists
    boolean existsByAccountNumber(String accountNumber);
}
