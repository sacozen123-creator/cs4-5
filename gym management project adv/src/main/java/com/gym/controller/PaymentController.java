package com.gym.controller;

import com.gym.service.GymService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
public class PaymentController {

    @Autowired
    private GymService gymService;

    // Simple checkout page without Stripe
    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam String type, 
                               @RequestParam String price,
                               Model model, 
                               Principal principal) {
        model.addAttribute("type", type);
        model.addAttribute("price", price);
        model.addAttribute("username", principal.getName());
        return "checkout";
    }

    // Process payment directly without Stripe
    @PostMapping("/process-payment")
    public String processPayment(@RequestParam String type,
                                 @RequestParam String username,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Activate membership directly
            gymService.purchaseMembership(username, type);
            redirectAttributes.addFlashAttribute("success", "✅ Payment successful! Your " + type + " membership is now active!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to activate membership: " + e.getMessage());
        }
        return "redirect:/membership";
    }
    
    // Payment cancelled
    @GetMapping("/payment-cancel")
    public String paymentCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Payment was cancelled. No charges were made.");
        return "redirect:/membership";
    }
    
    // Simple payment success page
    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam String type,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            gymService.purchaseMembership(principal.getName(), type);
            redirectAttributes.addFlashAttribute("success", "✅ Thank you! Your " + type + " membership is now active!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/membership";
    }
}