package com.example.e_commerce.service;

import com.example.e_commerce.entity.Category;
import com.example.e_commerce.entity.Favorites;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.CategoryRepository;
import com.example.e_commerce.repository.FavoritesRepository;
import com.example.e_commerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional // Sınıf seviyesinde transaction
public class FavoritesService {
    private final FavoritesRepository favRepo;
    private final UserService userService;
    private final ProductRepository prodRepo;
    private final CategoryRepository catRepo;
    private final UserInteractionService uiService;

    public FavoritesService(FavoritesRepository favRepo,
                            UserService userService,
                            ProductRepository prodRepo,
                            CategoryRepository catRepo,
                            UserInteractionService uiService) {
        this.favRepo = favRepo;
        this.userService = userService;
        this.prodRepo = prodRepo;
        this.catRepo = catRepo;
        this.uiService = uiService;
    }

    @Transactional
    public boolean toggleFavorite(String username, Long productId) {
        User user = userService.findByUsername(username).orElseThrow();
        Product p = prodRepo.findById(productId).orElseThrow();
        Category c = p.getCategory();

        Optional<Favorites> existing = favRepo.findByUserAndProduct(user, p);
        boolean added;
        if (existing.isPresent()) {
            favRepo.delete(existing.get()); // Güvenli delete metodu
            added = false;
            // Favorilerden çıkarma işlemini kayıt etmiyoruz
        } else {
            Favorites fav = new Favorites();
            fav.setUser(user);
            fav.setProduct(p);
            fav.setCategory(c);
            favRepo.save(fav);
            added = true;
            // Sadece favorilere EKLEME işlemini kayıt ediyoruz
            uiService.recordFavoriteAdd(username, productId, true);
        }
        return added;
    }

    @Transactional(readOnly = true)
    public List<Long> getFavoriteProductIds(String username) {
        return favRepo.findProductIdsByUsername(username);
    }

    @Transactional
    public boolean removeFavorite(String username, Long productId) {
        try {
            User user = userService.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("User not found: " + username)
            );
            Product product = prodRepo.findById(productId).orElseThrow(
                    () -> new RuntimeException("Product not found: " + productId)
            );

            Optional<Favorites> existing = favRepo.findByUserAndProduct(user, product);
            if (existing.isPresent()) {
                favRepo.delete(existing.get());
                favRepo.flush(); // Değişiklikleri hemen uygula

                // Favorilerden çıkarma işlemini kayıt etmiyoruz
                // uiService.recordFavoriteAdd(username, productId, false); // Bu satırı kaldırdık
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error removing favorite: " + e.getMessage(), e);
        }
    }
}
