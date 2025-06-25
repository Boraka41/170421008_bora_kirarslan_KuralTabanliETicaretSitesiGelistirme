package com.example.e_commerce.controller;

import com.example.e_commerce.dto.UserDto;
import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.Category;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.service.CartService;
import com.example.e_commerce.service.FavoritesService;
import com.example.e_commerce.service.UserInteractionService;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.*;
import java.math.BigDecimal;

@Controller
public class IndexController {

    private final ProductRepository productRepository;
    private final UserService userService;
    private final FavoritesService favoritesService;
    private final UserInteractionService uiService;
    private final CartService cartService;

    public IndexController(ProductRepository productRepository, UserService userService, FavoritesService favoritesService, UserInteractionService uiService, CartService cartService) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.favoritesService = favoritesService;
        this.uiService = uiService;
        this.cartService = cartService;
    }

    @GetMapping("/index")
    public ModelAndView index(Authentication auth) {
        List<Category> categories = productRepository.findDistinctCategories();

        String username = null;
        boolean hasDiscounts = false;
        int discountCount = 0;
        
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
            
            // Check if user has any discounts
            for (Category category : categories) {
                double discount = uiService.calculateCategoryDiscount(username, category.getId());
                if (discount > 0) {
                    hasDiscounts = true;
                    discountCount++;
                }
            }
        }

        ModelAndView modelAndView = new ModelAndView("index");  // "index" view'ını kullan
        modelAndView.addObject("categories", categories);  // Kategorileri modele ekle
        modelAndView.addObject("hasDiscounts", hasDiscounts);  // İndirim durumunu ekle
        modelAndView.addObject("discountCount", discountCount);  // İndirim sayısını ekle
        modelAndView.addObject("isLoggedIn", username != null);
        
        // Kullanıcının seviye bilgisini ekle
        if (username != null) {
            userService.findByUsername(username).ifPresent(user -> {
                String userLevel = calculateUserLevel(user.getTotalPurchaseAmount());
                modelAndView.addObject("userLevel", userLevel);
            });
        }

        return modelAndView;
    }

    @GetMapping("/products")
    public String getProductsByCategory(@RequestParam("category") Long categoryId,
                                        Model model,
                                        Authentication auth) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        model.addAttribute("products", products);

        Set<Long> favIds = Collections.emptySet();
        String username = null;
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
            favIds = new HashSet<>(favoritesService.getFavoriteProductIds(username));
        }
        model.addAttribute("favoriteIds", favIds);

        double discount = 0.0;
        if (username != null) {
            discount = uiService.calculateCategoryDiscount(username, categoryId);
        }
        model.addAttribute("discount", discount);

        return "products";
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @GetMapping("/login")
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping("/productdetails/{productId}")
    public ModelAndView productDetails(@PathVariable Long productId,
                                       Authentication auth) {
        ModelAndView mv = new ModelAndView("productdetails");
        Optional<Product> opt = productRepository.findById(productId);
        if (opt.isPresent()) {
            Product product = opt.get();
            mv.addObject("product", product);

            String username = null;
            if (auth != null && auth.isAuthenticated()
                    && !(auth instanceof AnonymousAuthenticationToken)) {
                username = ((UserDetails) auth.getPrincipal()).getUsername();
            }

            double discount = 0.0;
            if (username != null) {
                discount = uiService.calculateCategoryDiscount(username,
                        product.getCategory().getId());
            }
            mv.addObject("discount", discount);
        } else {
            mv.setViewName("error");
        }
        return mv;
    }

    @GetMapping("/discount-categories")
    public String getDiscountCategories(Model model, Authentication auth) {
        String username = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        }

        List<Category> categoriesWithDiscounts = new ArrayList<>();
        List<Map<String, Object>> categoriesWithDiscountDetails = new ArrayList<>();
        List<Category> allCategories = productRepository.findDistinctCategories();

        for (Category category : allCategories) {
            double discount = uiService.calculateCategoryDiscount(username, category.getId());
            if (discount > 0) {
                Map<String, Object> categoryDetails = new HashMap<>();
                categoryDetails.put("category", category);
                categoryDetails.put("discount", 100*discount);
                categoriesWithDiscountDetails.add(categoryDetails);
            }
        }

        model.addAttribute("discountCategories", categoriesWithDiscountDetails);
        return "discountCategories"; // This will use the updated view
    }

    private String calculateUserLevel(BigDecimal totalPurchaseAmount) {
        double amount = totalPurchaseAmount.doubleValue();
        
        if (amount >= 25000) return "ELMAS";
        else if (amount >= 10000) return "ALTIN";
        else if (amount >= 5000) return "GUMUS";
        else if (amount >= 1000) return "BRONZ";
        else return "STANDART";
    }

}
