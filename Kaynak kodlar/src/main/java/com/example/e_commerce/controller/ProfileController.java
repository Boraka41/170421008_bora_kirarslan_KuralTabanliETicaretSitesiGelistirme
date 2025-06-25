package com.example.e_commerce.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.UserService;
import com.example.e_commerce.service.UserInteractionService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.Optional;
import java.math.BigDecimal;

@Controller
public class ProfileController {

    private final UserService userService;
    private final UserInteractionService userInteractionService;

    public ProfileController(UserService userService, UserInteractionService userInteractionService) {
        this.userService = userService;
        this.userInteractionService = userInteractionService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        UserDetails currentUser = (UserDetails) authentication.getPrincipal();
        model.addAttribute("username", currentUser.getUsername());

        // userService'den kullanıcı verisini çekip modele koy
        userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
            model.addAttribute("user", user);
            
            // Kullanıcının toplam satın alma tutarını al
            BigDecimal totalPurchaseAmount = user.getTotalPurchaseAmount();
            model.addAttribute("totalPurchaseAmount", totalPurchaseAmount);
            
            // Seviye bilgilerini hesapla
            String userLevel = calculateUserLevel(totalPurchaseAmount);
            BigDecimal nextLevelAmount = getNextLevelAmount(totalPurchaseAmount);
            double progressPercentage = calculateProgressPercentage(totalPurchaseAmount);
            
            model.addAttribute("userLevel", userLevel);
            model.addAttribute("nextLevelAmount", nextLevelAmount);
            model.addAttribute("progressPercentage", progressPercentage);
        });

        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser, Principal principal) {
        String username = principal.getName();
        Optional<User> existingUserOpt = userService.findByUsername(username);
        if (existingUserOpt.isEmpty()) {
            return "redirect:/login?error";
        }

        User existingUser = existingUserOpt.get();
        Long userId = existingUser.getId();

        userService.updateOneUser(userId, updatedUser);

        return "redirect:/profile";

    }

    // Kullanıcının seviyesini hesapla
    private String calculateUserLevel(BigDecimal totalAmount) {
        double amount = totalAmount.doubleValue();
        
        if (amount >= 25000) return "ELMAS";
        else if (amount >= 10000) return "ALTIN";
        else if (amount >= 5000) return "GUMUS";
        else if (amount >= 1000) return "BRONZ";
        else return "STANDART";
    }
    
    // Bir sonraki seviye için gereken tutar
    private BigDecimal getNextLevelAmount(BigDecimal totalAmount) {
        double amount = totalAmount.doubleValue();
        
        if (amount >= 25000) return BigDecimal.valueOf(25000); // Elmas seviyesinde zaten
        else if (amount >= 10000) return BigDecimal.valueOf(25000);
        else if (amount >= 5000) return BigDecimal.valueOf(10000);
        else if (amount >= 1000) return BigDecimal.valueOf(5000);
        else return BigDecimal.valueOf(1000);
    }
    
    // Progress bar için yüzde hesapla
    private double calculateProgressPercentage(BigDecimal totalAmount) {
        double amount = totalAmount.doubleValue();
        
        if (amount >= 25000) return 100.0; // Elmas seviyesinde
        else if (amount >= 10000) {
            // Altın seviyesinde, Elmas'a olan progress
            return ((amount - 10000) / (25000 - 10000)) * 100.0;
        } else if (amount >= 5000) {
            // Gümüş seviyesinde, Altın'a olan progress
            return ((amount - 5000) / (10000 - 5000)) * 100.0;
        } else if (amount >= 1000) {
            // Bronz seviyesinde, Gümüş'e olan progress
            return ((amount - 1000) / (5000 - 1000)) * 100.0;
        } else {
            // Standart seviyesinde, Bronz'a olan progress
            return (amount / 1000) * 100.0;
        }
    }

}
