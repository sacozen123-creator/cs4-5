package com.gym.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String type; // BASIC, PREMIUM, VIP
    private Double price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // ACTIVE, EXPIRED, CANCELLED

    public Membership() {}

    public Membership(String username, String type, Double price, LocalDateTime startDate, LocalDateTime endDate) {
        this.username = username;
        this.type = type;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}