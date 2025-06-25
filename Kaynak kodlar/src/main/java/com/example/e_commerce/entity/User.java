package com.example.e_commerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.example.e_commerce.enums.UserLevelMultiplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 20)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(name = "full_name")
    private String fullName;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @NotNull
    @Column(nullable = false)
    private int age;

    @NotBlank
    @Column(nullable = false)
    private String gender;

    private boolean enabled = false;

    private String verificationToken;

    private LocalDateTime tokenExpiration;

    @Column(nullable = false, columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double score = 0.0;

    @Column(nullable = false, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal totalPurchaseAmount = BigDecimal.ZERO;

    public @NotBlank @Size(min = 3, max = 20) String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank @Size(min = 3, max = 20) String username) {
        this.username = username;
    }

    public @NotBlank String getFullName() {
        return fullName;
    }

    public void setFullName(@NotBlank String fullName) {
        this.fullName = fullName;
    }

    public @NotBlank @Size(min = 6) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(min = 6) String password) {
        this.password = password;
    }

    public @NotBlank @Email String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Email String email) {
        this.email = email;
    }

    public @NotBlank String getRole() {
        return role;
    }

    public void setRole(@NotBlank String role) {
        this.role = role;
    }

    @NotNull
    public int getAge() {
        return age;
    }

    public void setAge(@NotNull int age) {
        this.age = age;
    }

    public @NotBlank String getGender() {
        return gender;
    }

    public void setGender(@NotBlank String gender) {
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public LocalDateTime getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(LocalDateTime tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public BigDecimal getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public void setTotalPurchaseAmount(BigDecimal totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
    }

    // İndirim Hesaplama Metodları

    /**
     * Kullanıcının seviyesini belirler
     */
    public String getUserLevel() {
        double amount = this.totalPurchaseAmount != null ? this.totalPurchaseAmount.doubleValue() : 0.0;
        
        if (amount >= 25000) return "ELMAS";
        else if (amount >= 10000) return "ALTIN";
        else if (amount >= 5000) return "GUMUS";
        else if (amount >= 1000) return "BRONZ";
        else return "STANDART";
    }

    /**
     * Kullanıcının seviye çarpanını getirir
     */
    public UserLevelMultiplier getUserLevelMultiplier() {
        double amount = this.totalPurchaseAmount != null ? this.totalPurchaseAmount.doubleValue() : 0.0;
        return UserLevelMultiplier.fromPurchaseAmount(amount);
    }

    /**
     * Nihai indirim skorunu hesaplar (Score * Seviye Çarpanı)
     */
    public double calculateFinalDiscountScore() {
        double userScore = this.score != null ? this.score : 0.0;
        double levelMultiplier = getUserLevelMultiplier().getMultiplier();
        return userScore * levelMultiplier;
    }

    /**
     * İndirim yüzdesini hesaplar (%0-30 arası)
     */
    public double calculateDiscountPercentage() {
        double finalScore = calculateFinalDiscountScore();
        
        // Skorunu yüzdeye çevir (maksimum %30 indirim)
        double discountPercentage = Math.min(finalScore * 0.1, 30.0);
        
        // Minimum %1 indirim için skor kontrolü
        if (finalScore < 1.0) {
            return 0.0; // İndirim yok
        }
        
        return Math.round(discountPercentage * 100.0) / 100.0; // 2 ondalık hane
    }

    /**
     * İndirim almaya uygun mu kontrol eder
     */
    public boolean isEligibleForDiscount() {
        return calculateFinalDiscountScore() >= 1.0;
    }

    /**
     * İndirim tutarını hesaplar
     */
    public BigDecimal calculateDiscountAmount(BigDecimal originalPrice) {
        if (!isEligibleForDiscount() || originalPrice == null) {
            return BigDecimal.ZERO;
        }
        
        double discountPercentage = calculateDiscountPercentage();
        double discountAmount = originalPrice.doubleValue() * (discountPercentage / 100.0);
        
        return BigDecimal.valueOf(discountAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * İndirimli fiyatı hesaplar
     */
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        if (!isEligibleForDiscount() || originalPrice == null) {
            return originalPrice;
        }
        
        BigDecimal discountAmount = calculateDiscountAmount(originalPrice);
        return originalPrice.subtract(discountAmount);
    }

    /**
     * İndirim bilgilerini string olarak döndürür
     */
    public String getDiscountInfo() {
        if (!isEligibleForDiscount()) {
            return "İndirim yok";
        }
        
        double percentage = calculateDiscountPercentage();
        return String.format("%%%s indirim", percentage);
    }
}
