package com.example.e_commerce.controller;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.DiscountService;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private UserService userService;

    /**
     * Kullanıcının indirim detaylarını getirir
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getDiscountDetails(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> discountDetails = discountService.getUserDiscountDetails(user);
            return ResponseEntity.ok(discountDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * İndirim önerisi getirir
     */
    @GetMapping("/suggestion")
    public ResponseEntity<String> getDiscountSuggestion(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            String suggestion = discountService.generateDiscountSuggestion(user);
            return ResponseEntity.ok(suggestion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Seviye ilerlemesi bilgilerini getirir
     */
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getLevelProgress(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> progress = discountService.calculateLevelProgress(user);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Seviye bazlı indirim örneklerini getirir
     */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, String>> getDiscountExamples() {
        Map<String, String> examples = discountService.getLevelDiscountExamples();
        return ResponseEntity.ok(examples);
    }

    /**
     * Kullanıcının mevcut indirim durumunu test eder
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testUserDiscount(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> testResult = Map.of(
                "username", user.getUsername(),
                "userLevel", user.getUserLevel(),
                "userScore", user.getScore(),
                "totalPurchaseAmount", user.getTotalPurchaseAmount(),
                "levelMultiplier", user.getUserLevelMultiplier().getMultiplier(),
                "finalDiscountScore", user.calculateFinalDiscountScore(),
                "discountPercentage", user.calculateDiscountPercentage(),
                "isEligibleForDiscount", user.isEligibleForDiscount(),
                "discountInfo", user.getDiscountInfo()
            );

            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 