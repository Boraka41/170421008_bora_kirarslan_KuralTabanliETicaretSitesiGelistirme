package com.example.e_commerce.enums;

public enum InteractionType {
    PRODUCT_CLICK(0.0953),    // ürün tıklama
    DETAILS_EXPAND(0.1429),   // ürün detaylarını genişletme
    RATING(0.1905),           // ürün puanlama
    REVIEW_SUBMIT(0.2143),    // ürün yorumu yazma
    CART_ADD(0.2381),         // sepete ekleme
    FAVORITES_ADD(0.2857),    // favorilere ekleme
    PURCHASED(0.3809);        // satın alma
    

    private final double weight;

    InteractionType(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
