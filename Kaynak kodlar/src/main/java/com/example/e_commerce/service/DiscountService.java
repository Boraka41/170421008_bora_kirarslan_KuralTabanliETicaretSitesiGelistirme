package com.example.e_commerce.service;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.enums.UserLevelMultiplier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiscountService {

    /**
     * Kullanıcının detaylı indirim bilgilerini getirir
     */
    public Map<String, Object> getUserDiscountDetails(User user) {
        Map<String, Object> discountDetails = new HashMap<>();
        
        // Temel bilgiler
        discountDetails.put("userLevel", user.getUserLevel());
        discountDetails.put("userScore", user.getScore());
        discountDetails.put("levelMultiplier", user.getUserLevelMultiplier().getMultiplier());
        
        // Hesaplamalar
        discountDetails.put("finalDiscountScore", user.calculateFinalDiscountScore());
        discountDetails.put("discountPercentage", user.calculateDiscountPercentage());
        discountDetails.put("isEligibleForDiscount", user.isEligibleForDiscount());
        discountDetails.put("discountInfo", user.getDiscountInfo());
        
        return discountDetails;
    }

    /**
     * Ürün için indirimli fiyat hesaplar
     */
    public Map<String, Object> calculateProductDiscount(User user, Product product) {
        Map<String, Object> result = new HashMap<>();
        
        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountAmount = user.calculateDiscountAmount(originalPrice);
        BigDecimal discountedPrice = user.calculateDiscountedPrice(originalPrice);
        
        result.put("originalPrice", originalPrice);
        result.put("discountAmount", discountAmount);
        result.put("discountedPrice", discountedPrice);
        result.put("discountPercentage", user.calculateDiscountPercentage());
        result.put("hasDiscount", user.isEligibleForDiscount());
        
        return result;
    }

    /**
     * Kullanıcının seviye ilerlemesini hesaplar
     */
    public Map<String, Object> calculateLevelProgress(User user) {
        Map<String, Object> progress = new HashMap<>();
        
        String currentLevel = user.getUserLevel();
        double currentScore = user.getScore();
        UserLevelMultiplier currentMultiplier = user.getUserLevelMultiplier();
        
        // Bir sonraki seviye için gereken skor
        double nextLevelMultiplier = getNextLevelMultiplier(currentLevel);
        double requiredScoreForDiscount = 1.0 / currentMultiplier.getMultiplier(); // Minimum indirim için gereken skor
        
        progress.put("currentLevel", currentLevel);
        progress.put("currentScore", currentScore);
        progress.put("currentMultiplier", currentMultiplier.getMultiplier());
        progress.put("nextLevelMultiplier", nextLevelMultiplier);
        progress.put("requiredScoreForDiscount", requiredScoreForDiscount);
        progress.put("hasDiscount", currentScore >= requiredScoreForDiscount);
        
        return progress;
    }

    /**
     * İndirim önerisi oluşturur
     */
    public String generateDiscountSuggestion(User user) {
        if (user.isEligibleForDiscount()) {
            double percentage = user.calculateDiscountPercentage();
            return String.format("Tebrikler! Aktiviteleriniz sayesinde %%%s indirim kazandınız!", percentage);
        } else {
            String currentLevel = user.getUserLevel();
            UserLevelMultiplier multiplier = user.getUserLevelMultiplier();
            double requiredScore = 1.0 / multiplier.getMultiplier();
            double missingScore = requiredScore - user.getScore();
            
            return String.format("İndirim kazanmak için daha fazla aktif olun! " +
                    "%s seviyenizde %.1f puan daha toplamanız gerekiyor.", 
                    currentLevel, missingScore);
        }
    }

    /**
     * Seviye bazlı indirim örnekleri
     */
    public Map<String, String> getLevelDiscountExamples() {
        Map<String, String> examples = new HashMap<>();
        
        examples.put("STANDART", "100 puan = %1.5 indirim (Çarpan: 0.15)");
        examples.put("BRONZ", "25 puan = %10 indirim (Çarpan: 0.40)");
        examples.put("GUMUS", "18 puan = %10 indirim (Çarpan: 0.55)");
        examples.put("ALTIN", "13 puan = %10 indirim (Çarpan: 0.80)");
        examples.put("ELMAS", "10 puan = %10 indirim (Çarpan: 1.00)");
        
        return examples;
    }

    private double getNextLevelMultiplier(String currentLevel) {
        switch (currentLevel) {
            case "STANDART": return UserLevelMultiplier.BRONZ.getMultiplier();
            case "BRONZ": return UserLevelMultiplier.GUMUS.getMultiplier();
            case "GUMUS": return UserLevelMultiplier.ALTIN.getMultiplier();
            case "ALTIN": return UserLevelMultiplier.ELMAS.getMultiplier();
            case "ELMAS": return UserLevelMultiplier.ELMAS.getMultiplier();
            default: return UserLevelMultiplier.STANDART.getMultiplier();
        }
    }
} 