package com.example.e_commerce.controller;

import com.example.e_commerce.dto.ProductReviewDto;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.ProductReview;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.service.ProductReviewService;
import com.example.e_commerce.service.ProductService;
import com.example.e_commerce.service.UserInteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private ProductService productService;
    private ProductRepository productRepository;
    private UserInteractionService userInteractionService;
    private ProductReviewService productReviewService;

    public ProductController(ProductService productService, 
                           ProductRepository productRepository, 
                           UserInteractionService userInteractionService,
                           ProductReviewService productReviewService) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.userInteractionService = userInteractionService;
        this.productReviewService = productReviewService;
    }

    @GetMapping("/{productId}")
    public Optional<Product> getOneProduct(@PathVariable Long productId){
        return productService.getOneProduct(productId);
    }

    @PostMapping
    public Product createOneProduct(@RequestBody Product newProduct){
        return productService.createOneProduct(newProduct);
    }

    @PutMapping("/{productId}")
    public Product updateOneProduct(@PathVariable Long productId, @RequestBody Product updatedProduct){
        return productService.updateOneProduct(productId,updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public void deleteOneProduct(@PathVariable Long productId){
        productService.deleteOneProduct(productId);
    }

    @PostMapping("/{productId}/rate")
    public ResponseEntity<String> rateProduct(@PathVariable Long productId, 
                                            @RequestBody Map<String, Double> request,
                                            Authentication authentication) {
        try {
            logger.info("Rating request received for productId: {}", productId);
            
            String username = authentication.getName();
            Double rating = request.get("rating");
            
            logger.info("User: {}, Rating: {}", username, rating);
            
            if (rating == null || rating < 1 || rating > 5) {
                logger.warn("Invalid rating value: {}", rating);
                return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
            }
            
            userInteractionService.recordProductRating(username, productId, rating);
            productService.updateProductRating(productId);
            
            logger.info("Rating saved successfully for user: {}, product: {}, rating: {}", username, productId, rating);
            return ResponseEntity.ok("Rating saved successfully");
        } catch (Exception e) {
            logger.error("Error saving rating: ", e);
            return ResponseEntity.badRequest().body("Error saving rating: " + e.getMessage());
        }
    }

    @GetMapping("/{productId}/user-rating")
    public ResponseEntity<Double> getUserRating(@PathVariable Long productId, Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Getting user rating for user: {}, product: {}", username, productId);
            
            Double rating = userInteractionService.getUserRatingForProduct(username, productId);
            
            logger.info("User rating found: {}", rating);
            return ResponseEntity.ok(rating);
        } catch (Exception e) {
            logger.error("Error getting user rating: ", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{productId}/user-purchased")
    public ResponseEntity<Boolean> hasUserPurchased(@PathVariable Long productId, Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Checking purchase status for user: {}, product: {}", username, productId);
            
            boolean hasPurchased = userInteractionService.hasUserPurchasedProduct(username, productId);
            
            logger.info("User {} has {} purchased product {}", username, hasPurchased ? "" : "NOT", productId);
            return ResponseEntity.ok(hasPurchased);
        } catch (Exception e) {
            logger.error("Error checking purchase status: ", e);
            return ResponseEntity.badRequest().body(false);
        }
    }

    // Review endpoints
    @PostMapping("/{productId}/review")
    public ResponseEntity<String> addReview(@PathVariable Long productId, 
                                          @RequestBody Map<String, String> request,
                                          Authentication authentication) {
        try {
            String username = authentication.getName();
            String comment = request.get("comment");
            
            logger.info("Adding review for product: {}, user: {}", productId, username);
            
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Yorum boş olamaz");
            }
            
            productReviewService.addReview(username, productId, comment);
            return ResponseEntity.ok("Yorumunuz başarıyla eklendi");
        } catch (IllegalArgumentException e) {
            logger.warn("Review validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding review: ", e);
            return ResponseEntity.badRequest().body("Yorum eklenirken hata oluştu: " + e.getMessage());
        }
    }

    @PutMapping("/{productId}/review")
    public ResponseEntity<String> updateReview(@PathVariable Long productId, 
                                             @RequestBody Map<String, String> request,
                                             Authentication authentication) {
        try {
            String username = authentication.getName();
            String comment = request.get("comment");
            
            logger.info("Updating review for product: {}, user: {}", productId, username);
            
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Yorum boş olamaz");
            }
            
            productReviewService.updateReview(username, productId, comment);
            return ResponseEntity.ok("Yorumunuz başarıyla güncellendi");
        } catch (IllegalArgumentException e) {
            logger.warn("Review update validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating review: ", e);
            return ResponseEntity.badRequest().body("Yorum güncellenirken hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<List<ProductReviewDto>> getProductReviews(@PathVariable Long productId) {
        try {
            logger.info("Getting reviews for product: {}", productId);
            
            List<ProductReviewDto> reviews = productReviewService.getProductReviews(productId);
            logger.info("Found {} reviews for product {}", reviews.size(), productId);
            
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Error getting reviews: ", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{productId}/user-review")
    public ResponseEntity<ProductReviewDto> getUserReview(@PathVariable Long productId, Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Getting user review for product: {}, user: {}", productId, username);
            
            Optional<ProductReviewDto> review = productReviewService.getUserReviewForProduct(username, productId);
            return ResponseEntity.ok(review.orElse(null));
        } catch (Exception e) {
            logger.error("Error getting user review: ", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{productId}/review-count")
    public ResponseEntity<Long> getReviewCount(@PathVariable Long productId) {
        try {
            long count = productReviewService.getReviewCount(productId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error getting review count: ", e);
            return ResponseEntity.badRequest().body(0L);
        }
    }

    @PostMapping("/{productId}/details-expand")
    public ResponseEntity<String> recordDetailsExpand(@PathVariable Long productId, Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Recording details expand for user: {}, product: {}", username, productId);

            userInteractionService.recordDetailsExpand(username, productId);
            return ResponseEntity.ok("Detay genişletme kaydedildi");
        } catch (Exception e) {
            logger.error("Error recording details expand: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata oluştu");
        }
    }
}
