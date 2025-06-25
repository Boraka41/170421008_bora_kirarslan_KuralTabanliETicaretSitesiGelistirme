package com.example.e_commerce.enums;

public enum UserLevelMultiplier {
    STANDART(0.15),
    BRONZ(0.40),
    GUMUS(0.55),
    ALTIN(0.80),
    ELMAS(1.00);

    private final double multiplier;

    UserLevelMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Seviye adına göre enum değerini döndürür
     */
    public static UserLevelMultiplier fromLevelName(String levelName) {
        if (levelName == null) {
            return STANDART;
        }
        
        try {
            return valueOf(levelName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STANDART;
        }
    }

    /**
     * Satın alma tutarına göre seviyeyi belirler
     */
    public static UserLevelMultiplier fromPurchaseAmount(double amount) {
        if (amount >= 25000) return ELMAS;
        else if (amount >= 10000) return ALTIN;
        else if (amount >= 5000) return GUMUS;
        else if (amount >= 1000) return BRONZ;
        else return STANDART;
    }
} 