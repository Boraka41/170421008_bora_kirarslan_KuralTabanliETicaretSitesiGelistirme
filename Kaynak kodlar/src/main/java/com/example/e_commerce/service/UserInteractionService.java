    package com.example.e_commerce.service;

    import com.example.e_commerce.entity.*;
    import com.example.e_commerce.enums.InteractionType;
    import com.example.e_commerce.enums.UserLevelMultiplier;
    import com.example.e_commerce.repository.CategoryRepository;
    import com.example.e_commerce.repository.OrderHistoryRepository;
    import com.example.e_commerce.repository.ProductRepository;
    import com.example.e_commerce.repository.UserInteractionRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    public class UserInteractionService {

        private static final Logger logger = LoggerFactory.getLogger(UserInteractionService.class);

        private final UserInteractionRepository repo;
        private final UserService userService;
        private final CategoryRepository catRepo;
        private final ProductRepository prodRepo;
        @Autowired
        private OrderHistoryRepository orderHistoryRepository;
        public UserInteractionService(UserInteractionRepository repo,
                                      UserService userService,
                                      CategoryRepository catRepo,
                                      ProductRepository prodRepo) {
            this.repo = repo;
            this.userService = userService;
            this.catRepo = catRepo;
            this.prodRepo = prodRepo;
        }

        public void recordProductClick(String username,
                                       Long categoryId,
                                       Long productId,
                                       Double value) {
            User u = userService.findByUsername(username).orElseThrow();
            Category c = catRepo.findById(categoryId).orElse(null);
            Product p = prodRepo.findById(productId).orElseThrow();

            UserInteraction ui = new UserInteraction();
            ui.setUser(u);
            ui.setCategory(c);
            ui.setProduct(p);
            ui.setInteractionType(InteractionType.PRODUCT_CLICK);
            ui.setTimestamp(LocalDateTime.now());
            ui.setValue(value);
            repo.save(ui);
            
            // Kullanıcının skorunu güncelle
            updateUserTotalScore(username);
        }

        public void recordCartAdd(String username,
                                  Long productId,
                                  int quantity,
                                  Double value) {
            User u = userService.findByUsername(username).orElseThrow();
            Product p = prodRepo.findById(productId).orElseThrow();
            Category c = p.getCategory();

            UserInteraction ui = new UserInteraction();
            ui.setUser(u);
            ui.setCategory(c);
            ui.setProduct(p);
            ui.setInteractionType(InteractionType.CART_ADD);
            ui.setTimestamp(LocalDateTime.now());
            ui.setValue(value);
            repo.save(ui);
            
            // Kullanıcının skorunu güncelle
            updateUserTotalScore(username);
        }

        public void recordFavoriteAdd(String username, Long productId, boolean added) {
            User u = userService.findByUsername(username).orElseThrow();
            Product p = prodRepo.findById(productId).orElseThrow();
            Category c = p.getCategory();

            UserInteraction ui = new UserInteraction();
            ui.setUser(u);
            ui.setCategory(c);
            ui.setProduct(p);
            ui.setInteractionType(InteractionType.FAVORITES_ADD);
            ui.setTimestamp(LocalDateTime.now());
            ui.setValue(null);
            repo.save(ui);
            
            // Kullanıcının skorunu güncelle
            updateUserTotalScore(username);
        }

        public void recordProductPurchase(Long userId, Long productId, Double totalPrice) {
            User u = userService.findById(userId).orElseThrow();
            Product p = prodRepo.findById(productId).orElseThrow();
            Category c = p.getCategory();

            UserInteraction ui = new UserInteraction();
            ui.setUser(u);
            ui.setCategory(c);
            ui.setProduct(p);
            ui.setInteractionType(InteractionType.PURCHASED);
            ui.setTimestamp(LocalDateTime.now());
            ui.setValue(totalPrice);
            repo.save(ui);
            
            // Kullanıcının skorunu güncelle
            updateUserTotalScore(u.getUsername());
        }

        public void recordProductRating(String username, Long productId, Double rating) {
            logger.info("Recording product rating - User: {}, Product: {}, Rating: {}", username, productId, rating);
            
            try {
                // Önce kullanıcının bu ürünü satın alıp almadığını kontrol et
                if (!hasUserPurchasedProduct(username, productId)) {
                    logger.warn("User {} has not purchased product {}, cannot rate", username, productId);
                    throw new IllegalArgumentException("Bu ürünü sadece satın alan kullanıcılar değerlendirebilir.");
                }
                
                User u = userService.findByUsername(username).orElseThrow();
                Product p = prodRepo.findById(productId).orElseThrow();
                Category c = p.getCategory();

                logger.info("Found user: {}, product: {}", u.getUsername(), p.getProductName());

                // Önceki rating'i kontrol et
                UserInteraction existingRating = repo.findByUserAndProductAndInteractionType(u, p, InteractionType.RATING);
                
                if (existingRating != null) {
                    logger.info("Updating existing rating from {} to {}", existingRating.getValue(), rating);
                    // Mevcut rating'i güncelle
                    existingRating.setValue(rating);
                    existingRating.setTimestamp(LocalDateTime.now());
                    repo.save(existingRating);
                } else {
                    logger.info("Creating new rating record");
                    // Yeni rating ekle
                    UserInteraction ui = new UserInteraction();
                    ui.setUser(u);
                    ui.setCategory(c);
                    ui.setProduct(p);
                    ui.setInteractionType(InteractionType.RATING);
                    ui.setTimestamp(LocalDateTime.now());
                    ui.setValue(rating);
                    UserInteraction savedRating = repo.save(ui);
                    logger.info("Rating saved with ID: {}", savedRating.getId());
                }
                
                // Kullanıcının skorunu güncelle
                updateUserTotalScore(username);
                
                logger.info("Product rating recorded successfully");
            } catch (Exception e) {
                logger.error("Error recording product rating: ", e);
                throw e;
            }
        }

        public Double getUserRatingForProduct(String username, Long productId) {
            logger.info("Getting user rating - User: {}, Product: {}", username, productId);
            
            try {
                User u = userService.findByUsername(username).orElseThrow();
                Product p = prodRepo.findById(productId).orElseThrow();
                UserInteraction rating = repo.findByUserAndProductAndInteractionType(u, p, InteractionType.RATING);
                
                Double result = rating != null ? rating.getValue() : null;
                logger.info("User rating result: {}", result);
                return result;
            } catch (Exception e) {
                logger.error("Error getting user rating: ", e);
                throw e;
            }
        }

        public boolean hasUserPurchasedProduct(String username, Long productId) {
            logger.info("Checking if user has purchased product - User: {}, Product: {}", username, productId);
            
            try {
                User u = userService.findByUsername(username).orElseThrow();
                Product p = prodRepo.findById(productId).orElseThrow();
                
                // Kullanıcının bu ürünü satın alıp almadığını kontrol et
                UserInteraction purchase = repo.findByUserAndProductAndInteractionType(u, p, InteractionType.PURCHASED);
                
                boolean hasPurchased = purchase != null;
                logger.info("User {} has {} purchased product {}", username, hasPurchased ? "" : "NOT", productId);
                return hasPurchased;
            } catch (Exception e) {
                logger.error("Error checking user purchase: ", e);
                throw e;
            }
        }

        public void recordDetailsExpand(String username, Long productId) {
            logger.info("Recording details expand - User: {}, Product: {}", username, productId);
            
            try {
                User u = userService.findByUsername(username).orElseThrow();
                Product p = prodRepo.findById(productId).orElseThrow();
                Category c = p.getCategory();

                // Ürün detaylarını genişletme etkileşimini kaydet
                UserInteraction ui = new UserInteraction();
                ui.setUser(u);
                ui.setCategory(c);
                ui.setProduct(p);
                ui.setInteractionType(InteractionType.DETAILS_EXPAND);
                ui.setTimestamp(LocalDateTime.now());
                ui.setValue(1.0); // Her genişletme 1 puan değerinde
                
                UserInteraction savedInteraction = repo.save(ui);
                logger.info("Details expand interaction saved with ID: {}", savedInteraction.getId());
                
                // Kullanıcının skorunu güncelle
                updateUserTotalScore(username);
            } catch (Exception e) {
                logger.error("Error recording details expand: ", e);
                throw e;
            }
        }

        public void recordReviewSubmit(String username, Long productId) {
            logger.info("Recording review submit - User: {}, Product: {}", username, productId);
            
            try {
                User u = userService.findByUsername(username).orElseThrow();
                Product p = prodRepo.findById(productId).orElseThrow();
                Category c = p.getCategory();

                // Yorum yazma etkileşimini kaydet
                UserInteraction ui = new UserInteraction();
                ui.setUser(u);
                ui.setCategory(c);
                ui.setProduct(p);
                ui.setInteractionType(InteractionType.REVIEW_SUBMIT);
                ui.setTimestamp(LocalDateTime.now());
                ui.setValue(1.0); // Her yorum 1 puan değerinde
                
                UserInteraction savedInteraction = repo.save(ui);
                logger.info("Review submit interaction saved with ID: {}", savedInteraction.getId());
                
                // Kullanıcının skorunu güncelle
                updateUserTotalScore(username);
            } catch (Exception e) {
                logger.error("Error recording review submit: ", e);
                throw e;
            }
        }

        public boolean hasNotPurchasedInCategory(Long userId, Long categoryId) {
            List<OrderHistory> orders = orderHistoryRepository.findByUserIdAndCategoryId(userId, categoryId);
            return orders.isEmpty();
        }

        public double calculateCategoryDiscount(String username, Long categoryId) {
            User user = userService.findByUsername(username).orElseThrow();
            boolean hasNotPurchased = hasNotPurchasedInCategory(user.getId(), categoryId);

            List<UserInteraction> list = repo.findByUserUsernameAndCategoryId(username, categoryId);
            
            // Kullanıcının seviyesini al
            UserLevelMultiplier userLevel = getUserLevel(username);
            
            double score = list.stream()
                    .mapToDouble(ui -> ui.getInteractionType().getWeight() * userLevel.getMultiplier())
                    .sum();

            // Debug logging
            logger.info("=== DISCOUNT CALCULATION DEBUG ===");
            logger.info("Username: {}", username);
            logger.info("CategoryId: {}", categoryId);
            logger.info("User Level: {}", userLevel);
            logger.info("User Level Multiplier: {}", userLevel.getMultiplier());
            logger.info("Has Not Purchased in Category: {}", hasNotPurchased);
            logger.info("Total Interactions in Category: {}", list.size());
            logger.info("Calculated Score: {}", score);
            
            // Her etkileşimi detaylı göster
            for (UserInteraction ui : list) {
                double interactionScore = ui.getInteractionType().getWeight() * userLevel.getMultiplier();
                logger.info("Interaction: {} - Weight: {} - Multiplier: {} - Final Score: {}", 
                           ui.getInteractionType(), 
                           ui.getInteractionType().getWeight(), 
                           userLevel.getMultiplier(), 
                           interactionScore);
            }

            // Kullanıcının toplam skorunu güncelle
            updateUserTotalScore(username);

            // Eğer kullanıcı daha önce ürün satın almamış ise, %10 indirim uygulama
            if (hasNotPurchased && score > 5) {
                logger.info("Applying 10% discount for new customer (score: {})", score);
                return 0.10;
            }

            // Satın alım yapıldıysa etkileşim skoruna göre indirim hesapla
            if(!hasNotPurchased){
                if ( score > 5 && score < 17) {
                    logger.info("Applying 20% discount (score: {})", 100*score);
                    return 0.20;       // %20 indirim
                }
                else if (score >= 17 && score < 25) {
                    logger.info("Applying 30% discount (score: {})", 100*score);
                    return 0.30; // %30 indirim
                }
                else if (score >= 25 && score < 33) {
                    logger.info("Applying 40% discount (score: {})", 100*score);
                    return 0.40; // %40 indirim
                }
                else if (score > 33) {
                    logger.info("Applying 50% discount (score: {})", 100*score);
                    return 0.50;                // %50 indirim
                }
            }
            
            logger.info("No discount applied (score: {})", 100*score);
            logger.info("=== END DISCOUNT CALCULATION ===");
            return 0.0;
        }

        // Kullanıcının toplam skorunu hesapla ve güncelle
        public void updateUserTotalScore(String username) {
            User user = userService.findByUsername(username).orElseThrow();
            List<UserInteraction> allInteractions = repo.findByUser(user);
            
            // Kullanıcının seviyesini al
            UserLevelMultiplier userLevel = getUserLevel(username);
            
            double totalScore = allInteractions.stream()
                    .mapToDouble(ui -> ui.getInteractionType().getWeight() * userLevel.getMultiplier())
                    .sum();
            
            userService.updateUserScore(username, totalScore);
            logger.info("Updated total score for user {} (level: {}): {}", username, userLevel, totalScore);
        }

        // Kategoriye göre kullanıcının skorunu hesapla
        public double calculateUserCategoryScore(String username, Long categoryId) {
            List<UserInteraction> list = repo.findByUserUsernameAndCategoryId(username, categoryId);
            
            // Kullanıcının seviyesini al
            UserLevelMultiplier userLevel = getUserLevel(username);
            
            return list.stream()
                    .mapToDouble(ui -> ui.getInteractionType().getWeight() * userLevel.getMultiplier())
                    .sum();
        }

        // Kullanıcının toplam skorunu al
        public double getUserTotalScore(String username) {
            return userService.getUserScore(username);
        }

        /**
         * Kullanıcının toplam satın alma tutarını hesaplar
         */
        public double calculateUserPurchaseAmount(String username) {
            User user = userService.findByUsername(username).orElseThrow();
            List<UserInteraction> purchaseInteractions = repo.findByUserAndInteractionType(user, InteractionType.PURCHASED);
            
            return purchaseInteractions.stream()
                    .mapToDouble(ui -> ui.getValue() != null ? ui.getValue() : 0.0)
                    .sum();
        }

        /**
         * Kullanıcının seviyesini belirler
         */
        public UserLevelMultiplier getUserLevel(String username) {
            double totalPurchaseAmount = calculateUserPurchaseAmount(username);
            return UserLevelMultiplier.fromPurchaseAmount(totalPurchaseAmount);
        }

        // Tüm kullanıcıların skorlarını güncelle (initialization için)
        public void updateAllUsersScores() {
            logger.info("Updating scores for all users...");
            
            List<User> allUsers = userService.getAllUsers();
            
            for (User user : allUsers) {
                try {
                    List<UserInteraction> userInteractions = repo.findByUser(user);
                    
                    // Kullanıcının seviyesini al
                    UserLevelMultiplier userLevel = getUserLevel(user.getUsername());
                    
                    double totalScore = userInteractions.stream()
                            .mapToDouble(ui -> ui.getInteractionType().getWeight() * userLevel.getMultiplier())
                            .sum();
                    
                    userService.updateUserScore(user.getUsername(), totalScore);
                    logger.info("Updated score for user {} (level: {}): {}", user.getUsername(), userLevel, totalScore);
                } catch (Exception e) {
                    logger.error("Error updating score for user {}: ", user.getUsername(), e);
                }
            }
            
            logger.info("Finished updating scores for all users");
        }

    }
