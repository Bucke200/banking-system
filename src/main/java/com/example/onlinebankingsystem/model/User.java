package com.example.onlinebankingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users") // Explicitly name the table "users"
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates no-args constructor
@AllArgsConstructor // Lombok: Generates all-args constructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // Store hashed passwords

    @Column(nullable = false, unique = true)
    private String email;

    // Using ElementCollection for simple role management (e.g., "ROLE_USER", "ROLE_ADMIN")
    // For more complex role/permission systems, a separate Role entity might be better.
    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch roles as they are needed for security checks
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert roles (Set<String>) to Spring Security's GrantedAuthority
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // Password is already handled by the 'password' field from @Data

    // Username is already handled by the 'username' field from @Data

    @Override
    public boolean isAccountNonExpired() {
        return true; // Add logic if accounts can expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add logic for account locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add logic if credentials can expire
    }

    @Override
    public boolean isEnabled() {
        return true; // Add logic for disabling accounts
    }
}
