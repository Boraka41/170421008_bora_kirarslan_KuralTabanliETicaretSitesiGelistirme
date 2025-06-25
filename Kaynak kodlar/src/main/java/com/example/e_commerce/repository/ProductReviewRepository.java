package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.ProductReview;
import com.example.e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    
    // Belirli bir ürünün tüm yorumlarını al (en yeni önce)
    List<ProductReview> findByProductOrderByCreatedAtDesc(Product product);
    
    // Kullanıcının belirli bir ürün için yazdığı yorumu al
    Optional<ProductReview> findByUserAndProduct(User user, Product product);
    
    // Bir ürünün toplam yorum sayısını al
    long countByProduct(Product product);
    
    // Kullanıcının belirli bir ürün için yorum yapıp yapmadığını kontrol et
    boolean existsByUserAndProduct(User user, Product product);
} 