package com.gym.service;

import com.gym.model.*;
import com.gym.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import com.gym.repository.PasswordResetTokenRepo;
import com.gym.model.PasswordResetToken;
import java.util.UUID;
@Service
public class GymService {

    @Autowired 
    private UserRepo userRepo;
    
    @Autowired 
    private SessionRepo sessionRepo;
    
    @Autowired 
    private BookingRepo bookingRepo;
    
    @Autowired 
    private MembershipRepo membershipRepo;
    
    @Autowired 
    private MachineRepo machineRepo;
    
    @Autowired 
    private TransactionHistoryService transactionHistoryService;
    
    @Autowired 
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;

    // USER REGISTRATION
    
    public void register(String username, String password, String confirmPassword, 
                        String fullName, String email, String phone, String userType,
                        Integer age, Double weight, Integer height, String fitnessGoal,
                        String specialization, String certification, Integer experienceYears, String bio) {
        
        // Validation
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (userRepo.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepo.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Create user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone != null ? phone : "");
        user.setUserType(userType);
        user.setJoinedDate(LocalDate.now().toString());
        
        // Set role and specific fields based on user type
        if (username.equalsIgnoreCase("admin")) {
            user.setRole("ROLE_ADMIN");
        } else if ("TRAINER".equals(userType)) {
            user.setRole("ROLE_TRAINER");
            // Set trainer-specific fields
            user.setSpecialization(specialization);
            user.setCertification(certification);
            user.setExperienceYears(experienceYears);
            user.setBio(bio);
            // Set default values for customer fields
            user.setAge(0);
            user.setWeight(0.0);
            user.setHeight(0);
            user.setFitnessGoal("");
        } else {
            user.setRole("ROLE_USER");
            // Set customer-specific fields
            user.setAge(age != null ? age : 0);
            user.setWeight(weight != null ? weight : 0.0);
            user.setHeight(height != null ? height : 0);
            user.setFitnessGoal(fitnessGoal != null ? fitnessGoal : "");
            // Set default values for trainer fields
            user.setSpecialization("");
            user.setCertification("");
            user.setExperienceYears(0);
            user.setBio("");
        }
        
        userRepo.save(user);
        transactionHistoryService.recordTransaction(user.getUsername(), "REGISTER", "User registered as " + userType);
    }

    // SESSION MANAGEMENT
    
    public List<Session> getSessions() {
        return sessionRepo.findAll();
    }
    
    public long getTotalSessions() {
        return sessionRepo.count();
    }
    
    public void addSession(String name, String trainer) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Session name cannot be empty");
        }
        if (trainer == null || trainer.trim().isEmpty()) {
            throw new IllegalArgumentException("Trainer name cannot be empty");
        }
        
        // Check if session already exists
        List<Session> existing = sessionRepo.findAll();
        for (Session s : existing) {
            if (s.getName().equalsIgnoreCase(name) && s.getTrainer().equalsIgnoreCase(trainer)) {
                throw new IllegalArgumentException("This session already exists!");
            }
        }
        
        Session session = new Session();
        session.setName(name);
        session.setTrainer(trainer);
        sessionRepo.save(session);
        
        transactionHistoryService.recordTransaction("SYSTEM", "ADD_SESSION", 
            "Session: " + name + " by Trainer: " + trainer);
    }

    public void bookSession(String username, String sessionName, String trainer) {
        // Check if already booked
        List<Booking> existing = bookingRepo.findByUsername(username);
        
        for (Booking b : existing) {
            if (b.getSession().equalsIgnoreCase(sessionName) && 
                b.getTrainer().equalsIgnoreCase(trainer)) {
                throw new IllegalArgumentException("You already booked this session!");
            }
        }
        
        // Create new booking
        Booking booking = new Booking();
        booking.setUsername(username);
        booking.setSession(sessionName);
        booking.setTrainer(trainer);
        bookingRepo.save(booking);
        
        // Record transaction
        transactionHistoryService.recordTransaction(username, "BOOK_SESSION", 
            "Booked Session: " + sessionName + " with " + trainer);
        
        // NOTIFY THE TRAINER
        notifyTrainer(trainer, username, sessionName);
    }
    
    
    public List<Booking> getBookings(String username) {
        return bookingRepo.findByUsername(username);
    }
    
    public long getTotalBookings() {
        return bookingRepo.count();
    }
    
    public void deleteBooking(Long id, String username) {
        Optional<Booking> booking = bookingRepo.findById(id);
        if (booking.isPresent()) {
            Booking b = booking.get();
            String details = "Cancelled booking for Session: " + b.getSession();
            bookingRepo.deleteById(id);
            transactionHistoryService.recordTransaction(username, "CANCEL_BOOKING", details);
        }
    }
    
    // Get all bookings for sessions taught by a specific trainer
    public List<Booking> getTrainerBookings(String trainerName) {
        List<Booking> allBookings = bookingRepo.findAll();
        List<Booking> trainerBookings = new ArrayList<>();
        
        for (Booking booking : allBookings) {
            if (booking.getTrainer().equalsIgnoreCase(trainerName)) {
                trainerBookings.add(booking);
            }
        }
        return trainerBookings;
    }

    // MEMBERSHIP MANAGEMENT
    
    public Membership getActiveMembership(String username) {
        Optional<Membership> membership = membershipRepo.findByUsernameAndStatus(username, "ACTIVE");
        return membership.orElse(null);
    }
    
    public List<Membership> getUserMemberships(String username) {
        return membershipRepo.findByUsernameOrderByStartDateDesc(username);
    }
    
    public void purchaseMembership(String username, String type) {
        double price;
        int durationMonths;
        
        switch (type) {
            case "BASIC":
                price = 49.99;
                durationMonths = 1;
                break;
            case "PREMIUM":
                price = 129.99;
                durationMonths = 3;
                break;
            case "VIP":
                price = 499.99;
                durationMonths = 12;
                break;
            default:
                throw new IllegalArgumentException("Invalid membership type: " + type);
        }
        
        // Expire old membership
        Optional<Membership> oldMembership = membershipRepo.findByUsernameAndStatus(username, "ACTIVE");
        if (oldMembership.isPresent()) {
            Membership old = oldMembership.get();
            old.setStatus("EXPIRED");
            membershipRepo.save(old);
        }
        
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMonths(durationMonths);
        
        Membership membership = new Membership(username, type, price, start, end);
        membershipRepo.save(membership);
        
        transactionHistoryService.recordTransaction(username, "PURCHASE_MEMBERSHIP", 
            "Purchased " + type + " membership for $" + price);
    }

    // EQUIPMENT MANAGEMENT
    
    public List<Machine> getAllMachines() {
        return machineRepo.findAll();
    }
    
    public List<Machine> getMachinesByTrainer(String trainer) {
        return machineRepo.findByAssignedTrainer(trainer);
    }
    
    public void addMachine(String name, String type, String trainer) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Machine name cannot be empty");
        }
        
        Machine machine = new Machine();
        machine.setName(name);
        machine.setType(type);
        machine.setAssignedTrainer(trainer);
        machine.setStatus("AVAILABLE");
        machineRepo.save(machine);
        
        transactionHistoryService.recordTransaction(trainer, "ADD_MACHINE", 
            "Added machine: " + name);
    }
    
    public void updateMachineStatus(Long machineId, String status, String trainer) {
        Optional<Machine> machineOpt = machineRepo.findById(machineId);
        if (machineOpt.isPresent()) {
            Machine machine = machineOpt.get();
            if (machine.getAssignedTrainer().equals(trainer) || isAdmin(trainer)) {
                machine.setStatus(status);
                machineRepo.save(machine);
                transactionHistoryService.recordTransaction(trainer, "UPDATE_MACHINE", 
                    "Updated " + machine.getName() + " status to " + status);
            } else {
                throw new IllegalArgumentException("You don't have permission to update this machine");
            }
        }
    }

    // TRANSACTION HISTORY
    
    public List<TransactionHistory> getUserTransactions(String username) {
        return transactionHistoryService.getUserTransactions(username);
    }
    
    public List<TransactionHistory> getAllTransactions() {
        return transactionHistoryService.getAllTransactions();
    }

    //  USER MANAGEMENT FOR ADMIN
    
    public List<User> getAllCustomers() {
        return userRepo.findByUserType("CUSTOMER");
    }
    
    public List<User> getAllTrainers() {
        return userRepo.findByRole("ROLE_TRAINER");
    }
    
    public List<User> getRecentMembers() {
        return userRepo.findAllByOrderByJoinedDateDesc();
    }
    
    public long getTotalMembers() {
        return userRepo.findByUserType("CUSTOMER").size();
    }
    
    public long getTotalTrainers() {
        return userRepo.findByRole("ROLE_TRAINER").size();
    }
    
    public void assignTrainerToCustomer(Long customerId, Long trainerId) {
        Optional<User> customer = userRepo.findById(customerId);
        Optional<User> trainer = userRepo.findById(trainerId);
        
        if (customer.isPresent() && trainer.isPresent()) {
            transactionHistoryService.recordTransaction("SYSTEM", "ASSIGN_TRAINER", 
                "Assigned trainer " + trainer.get().getFullName() + " to customer " + customer.get().getFullName());
        } else {
            throw new IllegalArgumentException("Customer or Trainer not found");
        }
    }

    // ROLE CHECKING
    
    public boolean isAdmin(String username) {
        User user = userRepo.findByUsername(username);
        return user != null && "ROLE_ADMIN".equals(user.getRole());
    }
    
    public boolean isTrainer(String username) {
        User user = userRepo.findByUsername(username);
        return user != null && "ROLE_TRAINER".equals(user.getRole());
    }// TRAINER SESSION METHODS

 // Get sessions created by a specific trainer
 public List<Session> getSessionsByTrainer(String trainerName) {
     List<Session> allSessions = sessionRepo.findAll();
     List<Session> trainerSessions = new ArrayList<>();
     for (Session session : allSessions) {
         if (session.getTrainer().equalsIgnoreCase(trainerName)) {
             trainerSessions.add(session);
         }
     }
     return trainerSessions;
 }//  NOTIFICATION SYSTEM

//Send notification to trainer when customer books their session
public void notifyTrainer(String trainerName, String customerName, String sessionName) {
  String message = "📢 " + customerName + " has booked your session: " + sessionName;
  transactionHistoryService.recordTransaction(trainerName, "BOOKING_NOTIFICATION", message);
  
  // Also store in a notification list
  System.out.println("🔔 NOTIFICATION to " + trainerName + ": " + message);
}

//Get notifications for a trainer
public List<String> getTrainerNotifications(String trainerName) {
  List<TransactionHistory> transactions = transactionHistoryService.getUserTransactions(trainerName);
  List<String> notifications = new ArrayList<>();
  for (TransactionHistory t : transactions) {
      if ("BOOKING_NOTIFICATION".equals(t.getAction())) {
          notifications.add(t.getDetails());
      }
  }
  return notifications;
}

 // Trainer creates a new session
 public void addTrainerSession(String name, String trainerName) {
     if (name == null || name.trim().isEmpty()) {
         throw new IllegalArgumentException("Session name cannot be empty");
     }
     
     Session session = new Session();
     session.setName(name);
     session.setTrainer(trainerName);
     sessionRepo.save(session);
     
     transactionHistoryService.recordTransaction(trainerName, "CREATE_SESSION", 
         "Created session: " + name);
 }

 // Trainer deletes their own session
 public void deleteTrainerSession(Long sessionId, String trainerName) {
     Optional<Session> sessionOpt = sessionRepo.findById(sessionId);
     if (sessionOpt.isPresent()) {
         Session session = sessionOpt.get();
         if (session.getTrainer().equalsIgnoreCase(trainerName)) {
             // Delete all bookings for this session first
             List<Booking> allBookings = bookingRepo.findAll();
             for (Booking booking : allBookings) {
                 if (booking.getSession().equalsIgnoreCase(session.getName()) && 
                     booking.getTrainer().equalsIgnoreCase(trainerName)) {
                     bookingRepo.delete(booking);
                 }
             }
             sessionRepo.deleteById(sessionId);
             transactionHistoryService.recordTransaction(trainerName, "DELETE_SESSION", 
                 "Deleted session: " + session.getName());
         } else {
             throw new IllegalArgumentException("You can only delete your own sessions!");
         }
     }
 }
   //  FORGOT PASSWORD

public void createPasswordResetToken(String email) {
    User user = userRepo.findByEmail(email);
    if (user == null) {
        throw new IllegalArgumentException("No account found with this email: " + email);
    }
    
    // Delete old tokens for this user
    passwordResetTokenRepo.deleteByUsername(user.getUsername());
    
    // Create new token
    String token = UUID.randomUUID().toString();
    LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
    
    PasswordResetToken resetToken = new PasswordResetToken(token, user.getUsername(), expiryDate);
    passwordResetTokenRepo.save(resetToken);
    
    // Print token to console (since no email configured)
    System.out.println("");
    System.out.println("╔══════════════════════════════════════════════════════════════╗");
    System.out.println("║              PASSWORD RESET REQUEST                          ║");
    System.out.println("╠══════════════════════════════════════════════════════════════╣");
    System.out.println("║ Username: " + user.getUsername());
    System.out.println("║ Email: " + email);
    System.out.println("║ Token: " + token);
    System.out.println("║                                                              ║");
    System.out.println("║ Reset Link: http://localhost:8081/reset-password?token=" + token);
    System.out.println("╚══════════════════════════════════════════════════════════════╝");
    System.out.println("");
}

public boolean validateResetToken(String token) {
    Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepo.findByToken(token);
    if (resetTokenOpt.isEmpty()) {
        System.out.println("Token not found: " + token);
        return false;
    }
    
    PasswordResetToken resetToken = resetTokenOpt.get();
    if (resetToken.isExpired()) {
        System.out.println("Token expired: " + token);
        return false;
    }
    if (resetToken.isUsed()) {
        System.out.println("Token already used: " + token);
        return false;
    }
    
    return true;
}

public void resetPassword(String token, String newPassword, String confirmPassword) {
    if (!newPassword.equals(confirmPassword)) {
        throw new IllegalArgumentException("Passwords do not match");
    }
    if (newPassword.length() < 6) {
        throw new IllegalArgumentException("Password must be at least 6 characters");
    }
    
    Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepo.findByToken(token);
    if (resetTokenOpt.isEmpty()) {
        throw new IllegalArgumentException("Invalid reset token");
    }
    
    PasswordResetToken resetToken = resetTokenOpt.get();
    if (resetToken.isExpired()) {
        throw new IllegalArgumentException("Reset token has expired. Please request a new one.");
    }
    if (resetToken.isUsed()) {
        throw new IllegalArgumentException("Reset token has already been used.");
    }
    
    User user = userRepo.findByUsername(resetToken.getUsername());
    if (user == null) {
        throw new IllegalArgumentException("User not found");
    }
    
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepo.save(user);
    
    resetToken.setUsed(true);
    passwordResetTokenRepo.save(resetToken);
    
    transactionHistoryService.recordTransaction(user.getUsername(), "PASSWORD_RESET", "Password was reset successfully");
    
    System.out.println("✅ Password reset successful for user: " + user.getUsername());
}}