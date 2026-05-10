package com.gym.model;

import jakarta.persistence.*;

@Entity
public class Machine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String type; // CARDIO, STRENGTH, WEIGHTS
    private String status; // AVAILABLE, MAINTENANCE, IN_USE
    private String assignedTrainer;
    
    public Machine() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getAssignedTrainer() { return assignedTrainer; }
    public void setAssignedTrainer(String assignedTrainer) { this.assignedTrainer = assignedTrainer; }
}