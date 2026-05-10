package com.gym.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String role; // ROLE_ADMIN, ROLE_USER, ROLE_TRAINER
    private String userType; // CUSTOMER, TRAINER
    
    // Customer-only fields
    private Integer age;
    private Double weight;
    private Integer height;
    private String fitnessGoal;
    
    // Trainer-only fields
    private String specialization; // Yoga
    private String certification;
    private Integer experienceYears;
    private String bio;
    
    private String joinedDate;
    
    // Default constructor
    public User() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    // Customer fields
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    
    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }
    
    // Trainer fields
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }
    
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public String getJoinedDate() { return joinedDate; }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }
}