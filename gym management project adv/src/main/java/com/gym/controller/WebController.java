package com.gym.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.gym.model.*;
import com.gym.service.GymService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;

@Controller
public class WebController {

    @Autowired 
    private GymService service;

    //AUTHENTICATION PAGES
    
    @GetMapping("/login")
    public String login() { 
        return "login"; 
    }

    @GetMapping("/register")
    public String registerPage(Model m) {
        m.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "CUSTOMER") String userType,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) String fitnessGoal,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String certification,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) String bio,
            RedirectAttributes redirectAttributes) {
        
        try {
            service.register(username, password, confirmPassword, fullName, email, 
                            phone, userType, age, weight, height, fitnessGoal,
                            specialization, certification, experienceYears, bio);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // DASHBOARD
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model m, Authentication auth) {
        String username = auth.getName();
        
        // Get user role from authentication
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTrainer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER"));
        
        m.addAttribute("user", username);
        m.addAttribute("isAdmin", isAdmin);
        m.addAttribute("isTrainer", isTrainer);
        
        if (isAdmin) {
            // ADMIN DASHBOARD
            m.addAttribute("totalMembers", service.getTotalMembers());
            m.addAttribute("totalSessions", service.getTotalSessions());
            m.addAttribute("totalBookings", service.getTotalBookings());
            m.addAttribute("totalTrainers", service.getTotalTrainers());
            m.addAttribute("recentMembers", service.getRecentMembers());
            m.addAttribute("customers", service.getAllCustomers());
            m.addAttribute("trainers", service.getAllTrainers());
            m.addAttribute("sessions", service.getSessions());
            
            // Empty lists for other sections
            m.addAttribute("mySessions", new ArrayList<>());
            m.addAttribute("trainerBookings", new ArrayList<>());
            m.addAttribute("availableSessions", new ArrayList<>());
            m.addAttribute("bookings", new ArrayList<>());
            m.addAttribute("membership", null);
            m.addAttribute("notifications", new ArrayList<>());
            
        } else if (isTrainer) {
            // TRAINER DASHBOARD
            m.addAttribute("mySessions", service.getSessionsByTrainer(username));
            m.addAttribute("trainerBookings", service.getTrainerBookings(username));
            m.addAttribute("sessions", service.getSessions());
            m.addAttribute("notifications", service.getTrainerNotifications(username));
            
            // Empty lists for other sections
            m.addAttribute("totalMembers", 0);
            m.addAttribute("totalSessions", 0);
            m.addAttribute("totalBookings", 0);
            m.addAttribute("totalTrainers", 0);
            m.addAttribute("recentMembers", new ArrayList<>());
            m.addAttribute("customers", new ArrayList<>());
            m.addAttribute("trainers", new ArrayList<>());
            m.addAttribute("availableSessions", new ArrayList<>());
            m.addAttribute("bookings", new ArrayList<>());
            m.addAttribute("membership", null);
            
        } else {
            // CUSTOMER DASHBOARD
            m.addAttribute("availableSessions", service.getSessions());
            m.addAttribute("bookings", service.getBookings(username));
            m.addAttribute("membership", service.getActiveMembership(username));
            m.addAttribute("sessions", service.getSessions());
            
            // Empty lists for other sections
            m.addAttribute("totalMembers", 0);
            m.addAttribute("totalSessions", 0);
            m.addAttribute("totalBookings", 0);
            m.addAttribute("totalTrainers", 0);
            m.addAttribute("recentMembers", new ArrayList<>());
            m.addAttribute("customers", new ArrayList<>());
            m.addAttribute("trainers", new ArrayList<>());
            m.addAttribute("mySessions", new ArrayList<>());
            m.addAttribute("trainerBookings", new ArrayList<>());
            m.addAttribute("notifications", new ArrayList<>());
        }
        
        return "dashboard";
    }

    // CUSTOMER: BOOK SESSION
    
    @PostMapping("/book")
    public String bookSession(@RequestParam String sessionTrainer,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            String[] parts = sessionTrainer.split("\\|");
            String session = parts[0];
            String trainer = parts[1];
            service.bookSession(auth.getName(), session, trainer);
            redirectAttributes.addFlashAttribute("success", "✅ Session booked successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/deleteBooking")
    public String deleteBooking(@RequestParam Long id, Authentication auth) {
        service.deleteBooking(id, auth.getName());
        return "redirect:/dashboard";
    }

    // ADMIN: SESSION MANAGEMENT
    
    @PostMapping("/addSession")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAddSession(@RequestParam String name, 
                                  @RequestParam String trainer,
                                  RedirectAttributes redirectAttributes) {
        try {
            service.addSession(name, trainer);
            redirectAttributes.addFlashAttribute("success", "✅ Session '" + name + "' created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // TRAINER: SESSION MANAGEMENT
    
    @PostMapping("/trainer/addSession")
    public String trainerAddSession(@RequestParam String name, 
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            service.addTrainerSession(name, auth.getName());
            redirectAttributes.addFlashAttribute("success", "✅ Session '" + name + "' created successfully! Customers can now book it.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/trainer/deleteSession")
    public String trainerDeleteSession(@RequestParam Long sessionId, 
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        try {
            service.deleteTrainerSession(sessionId, auth.getName());
            redirectAttributes.addFlashAttribute("success", "✅ Session deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // MEMBERSHIP
    
    @GetMapping("/membership")
    public String membershipPage(Model m, Authentication auth) {
        String username = auth.getName();
        m.addAttribute("membership", service.getActiveMembership(username));
        m.addAttribute("membershipHistory", service.getUserMemberships(username));
        return "membership";
    }

    @PostMapping("/purchaseMembership")
    public String purchaseMembership(@RequestParam String type, 
                                     Authentication auth, 
                                     RedirectAttributes redirectAttributes) {
        try {
            service.purchaseMembership(auth.getName(), type);
            redirectAttributes.addFlashAttribute("success", "🎉 " + type + " membership activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/membership";
    }

    // EQUIPMENT MANAGEMENT
    
    @GetMapping("/machines")
    public String machinesPage(Model m, Authentication auth) {
        String username = auth.getName();
        boolean isAdmin = service.isAdmin(username);
        boolean isTrainer = service.isTrainer(username);
        
        if (isAdmin) {
            m.addAttribute("machines", service.getAllMachines());
        } else if (isTrainer) {
            m.addAttribute("machines", service.getMachinesByTrainer(username));
        } else {
            return "redirect:/dashboard";
        }
        
        m.addAttribute("isAdmin", isAdmin);
        m.addAttribute("isTrainer", isTrainer);
        return "machines";
    }

    @PostMapping("/addMachine")
    public String addMachine(@RequestParam String name, 
                             @RequestParam String type,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            service.addMachine(name, type, auth.getName());
            redirectAttributes.addFlashAttribute("success", "✅ Machine '" + name + "' added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/machines";
    }

    @PostMapping("/updateMachineStatus")
    public String updateMachineStatus(@RequestParam Long machineId, 
                                      @RequestParam String status,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        try {
            service.updateMachineStatus(machineId, status, auth.getName());
            redirectAttributes.addFlashAttribute("success", "✅ Machine status updated to " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/machines";
    }

    // TRANSACTION HISTORY
    
    @GetMapping("/history")
    public String historyPage(Model m, Authentication auth) {
        String username = auth.getName();
        boolean isAdmin = service.isAdmin(username);
        
        if (isAdmin) {
            m.addAttribute("transactions", service.getAllTransactions());
        } else {
            m.addAttribute("transactions", service.getUserTransactions(username));
        }
        
        m.addAttribute("isAdmin", isAdmin);
        return "history";
    }

    // ADMIN USER MANAGE
    
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public String customersPage(Model m, Authentication auth) {
        if (!service.isAdmin(auth.getName())) {
            return "redirect:/dashboard";
        }
        m.addAttribute("customers", service.getAllCustomers());
        return "customers";
    }

    @GetMapping("/trainers")
    @PreAuthorize("hasRole('ADMIN')")
    public String trainersPage(Model m, Authentication auth) {
        if (!service.isAdmin(auth.getName())) {
            return "redirect:/dashboard";
        }
        m.addAttribute("trainers", service.getAllTrainers());
        return "trainers";
    }

    @PostMapping("/assignTrainer")
    @PreAuthorize("hasRole('ADMIN')")
    public String assignTrainer(@RequestParam Long customerId, 
                                @RequestParam Long trainerId,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        if (!service.isAdmin(auth.getName())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access!");
            return "redirect:/dashboard";
        }
        try {
            service.assignTrainerToCustomer(customerId, trainerId);
            redirectAttributes.addFlashAttribute("success", "✅ Trainer assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // FORGOT PASSWORD
    
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, 
                                 RedirectAttributes redirectAttributes) {
        try {
            service.createPasswordResetToken(email);
            redirectAttributes.addFlashAttribute("success", 
                "📧 Password reset link has been sent. Check console for token.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model m) {
        if (service.validateResetToken(token)) {
            m.addAttribute("token", token);
            return "reset-password";
        } else {
            return "redirect:/forgot-password?error=Invalid or expired token";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            service.resetPassword(token, password, confirmPassword);
            redirectAttributes.addFlashAttribute("success", 
                "✅ Password reset successful! Please login with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}