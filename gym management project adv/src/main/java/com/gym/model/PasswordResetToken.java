package com.gym.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String token;
    
    private String username;
    private LocalDateTime expiryDate;
    private boolean used;
    
    public PasswordResetToken() {}
    
    public PasswordResetToken(String token, String username, LocalDateTime expiryDate) {
        this.token = token;
        this.username = username;
        this.expiryDate = expiryDate;
        this.used = false;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}