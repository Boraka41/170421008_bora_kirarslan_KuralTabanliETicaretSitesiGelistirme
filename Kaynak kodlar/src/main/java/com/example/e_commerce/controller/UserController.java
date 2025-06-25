package com.example.e_commerce.controller;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.UserService;
import com.example.e_commerce.service.UserInteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private UserInteractionService userInteractionService;

    public UserController(UserService userService, UserInteractionService userInteractionService) {
        this.userService = userService;
        this.userInteractionService = userInteractionService;
    }

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public Optional<User> getOneUser(@PathVariable Long userId){
        return userService.getOneUser(userId);
    }

    @PostMapping
    public User createOneUser(@RequestBody User newUser){
        return userService.createOneUser(newUser);
    }

    @PutMapping("/{userId}")
    public User updateOneUser(@PathVariable Long userId, @RequestBody User updatedUser){
        return userService.updateOneUser(userId,updatedUser);
    }

    @DeleteMapping("/{userId}")
    public void deleteOneUser(@PathVariable Long userId){
        userService.deleteOneUser(userId);
    }

    // Tüm kullanıcıların skorlarını güncelle (Admin yetkisi gerekli)
    @PostMapping("/update-scores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateAllUsersScores() {
        try {
            userInteractionService.updateAllUsersScores();
            return ResponseEntity.ok("Tüm kullanıcı skorları başarıyla güncellendi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Skor güncelleme hatası: " + e.getMessage());
        }
    }

    // Belirli bir kullanıcının skorunu al
    @GetMapping("/{username}/score")
    public ResponseEntity<Double> getUserScore(@PathVariable String username) {
        try {
            double score = userInteractionService.getUserTotalScore(username);
            return ResponseEntity.ok(score);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0.0);
        }
    }

    // Tüm kullanıcıların toplam satın alma tutarlarını güncelle (Admin yetkisi gerekli)
    @PostMapping("/update-purchase-amounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateAllUsersPurchaseAmounts() {
        try {
            userService.recalculateAllUsersPurchaseAmounts();
            return ResponseEntity.ok("Tüm kullanıcı satın alma tutarları başarıyla güncellendi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Satın alma tutarı güncelleme hatası: " + e.getMessage());
        }
    }

    // Belirli bir kullanıcının toplam satın alma tutarını al
    @GetMapping("/{username}/purchase-amount")
    public ResponseEntity<BigDecimal> getUserPurchaseAmount(@PathVariable String username) {
        try {
            BigDecimal amount = userService.getTotalPurchaseAmount(username);
            return ResponseEntity.ok(amount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(BigDecimal.ZERO);
        }
    }

    // Belirli bir kullanıcının toplam satın alma tutarını yeniden hesapla
    @PostMapping("/{username}/recalculate-purchase-amount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> recalculateUserPurchaseAmount(@PathVariable String username) {
        try {
            userService.recalculateTotalPurchaseAmount(username);
            return ResponseEntity.ok("Kullanıcının satın alma tutarı başarıyla yeniden hesaplandı.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Satın alma tutarı hesaplama hatası: " + e.getMessage());
        }
    }
}
