package com.example.e_commerce.service;

import com.example.e_commerce.dto.ProductReviewDto;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.ProductReview;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.ProductReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ProductReviewService.class);

    private final ProductReviewRepository reviewRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final UserInteractionService userInteractionService;

    public ProductReviewService(ProductReviewRepository reviewRepository, 
                               UserService userService, 
                               ProductRepository productRepository,
                               UserInteractionService userInteractionService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.productRepository = productRepository;
        this.userInteractionService = userInteractionService;
    }

    public ProductReview addReview(String username, Long productId, String comment) {
        logger.info("Adding review - User: {}, Product: {}, Comment length: {}", username, productId, comment.length());

        try {
            // Önce kullanıcının bu ürünü satın alıp almadığını kontrol et
            if (!userInteractionService.hasUserPurchasedProduct(username, productId)) {
                logger.warn("User {} has not purchased product {}, cannot add review", username, productId);
                throw new IllegalArgumentException("Bu ürüne sadece satın alan kullanıcılar yorum yapabilir.");
            }

            User user = userService.findByUsername(username).orElseThrow();
            Product product = productRepository.findById(productId).orElseThrow();

            // Kullanıcının daha önce bu ürün için yorum yapıp yapmadığını kontrol et
            if (reviewRepository.existsByUserAndProduct(user, product)) {
                logger.warn("User {} has already reviewed product {}", username, productId);
                throw new IllegalArgumentException("Bu ürün için zaten yorum yapmışsınız. Her ürün için sadece 1 yorum yapabilirsiniz.");
            }

            // Yorum uzunluğunu kontrol et
            if (comment == null || comment.trim().isEmpty()) {
                throw new IllegalArgumentException("Yorum boş olamaz.");
            }
            if (comment.length() > 1000) {
                throw new IllegalArgumentException("Yorum en fazla 1000 karakter olabilir.");
            }

            ProductReview review = new ProductReview();
            review.setUser(user);
            review.setProduct(product);
            review.setComment(comment.trim());
            review.setCreatedAt(LocalDateTime.now());

            ProductReview savedReview = reviewRepository.save(review);
            logger.info("Review saved with ID: {}", savedReview.getId());

            // Yorum yazma etkileşimini kaydet
            try {
                userInteractionService.recordReviewSubmit(username, productId);
            } catch (Exception e) {
                logger.warn("Failed to record review submit interaction: {}", e.getMessage());
                // Yorum kaydı başarılı oldu, etkileşim kaydı başarısız olsa bile devam et
            }

            return savedReview;
        } catch (Exception e) {
            logger.error("Error adding review: ", e);
            throw e;
        }
    }

    public ProductReview updateReview(String username, Long productId, String comment) {
        logger.info("Updating review - User: {}, Product: {}", username, productId);

        try {
            User user = userService.findByUsername(username).orElseThrow();
            Product product = productRepository.findById(productId).orElseThrow();

            Optional<ProductReview> existingReview = reviewRepository.findByUserAndProduct(user, product);
            if (existingReview.isEmpty()) {
                throw new IllegalArgumentException("Güncellenecek yorum bulunamadı.");
            }

            // Yorum uzunluğunu kontrol et
            if (comment == null || comment.trim().isEmpty()) {
                throw new IllegalArgumentException("Yorum boş olamaz.");
            }
            if (comment.length() > 1000) {
                throw new IllegalArgumentException("Yorum en fazla 1000 karakter olabilir.");
            }

            ProductReview review = existingReview.get();
            review.setComment(comment.trim());
            review.setUpdatedAt(LocalDateTime.now());

            ProductReview updatedReview = reviewRepository.save(review);
            logger.info("Review updated with ID: {}", updatedReview.getId());

            return updatedReview;
        } catch (Exception e) {
            logger.error("Error updating review: ", e);
            throw e;
        }
    }

    public List<ProductReviewDto> getProductReviews(Long productId) {
        logger.info("Getting reviews for product: {}", productId);
        
        try {
            Product product = productRepository.findById(productId).orElseThrow();
            List<ProductReview> reviews = reviewRepository.findByProductOrderByCreatedAtDesc(product);
            
            return reviews.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting product reviews: ", e);
            throw e;
        }
    }

    public Optional<ProductReviewDto> getUserReviewForProduct(String username, Long productId) {
        logger.info("Getting user review - User: {}, Product: {}", username, productId);

        try {
            User user = userService.findByUsername(username).orElseThrow();
            Product product = productRepository.findById(productId).orElseThrow();
            
            Optional<ProductReview> review = reviewRepository.findByUserAndProduct(user, product);
            return review.map(this::convertToDto);
        } catch (Exception e) {
            logger.error("Error getting user review: ", e);
            return Optional.empty();
        }
    }

    public boolean hasUserReviewedProduct(String username, Long productId) {
        try {
            User user = userService.findByUsername(username).orElseThrow();
            Product product = productRepository.findById(productId).orElseThrow();
            
            return reviewRepository.existsByUserAndProduct(user, product);
        } catch (Exception e) {
            logger.error("Error checking user review status: ", e);
            return false;
        }
    }

    public long getReviewCount(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        return reviewRepository.countByProduct(product);
    }

    private ProductReviewDto convertToDto(ProductReview review) {
        return new ProductReviewDto(
                review.getId(),
                review.getUser().getUsername(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
} 