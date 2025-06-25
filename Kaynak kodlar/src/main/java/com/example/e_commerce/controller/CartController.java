package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Cart;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.service.CartService;
import com.example.e_commerce.service.UserInteractionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ui.Model;


@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserInteractionService userInteractionService;

    public CartController(CartService cartService, UserInteractionService userInteractionService) {
        this.cartService = cartService;
        this.userInteractionService = userInteractionService;
    }

    @GetMapping("/{userId}")
    public Cart getCartByUserId(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }

    @PostMapping
    public CartItem addItemToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        return cartService.addItemToCart(userId, productId, quantity);
    }

    @GetMapping
    public String showCart(Principal principal, Model model) {
        String username = principal.getName();
        Cart cart = cartService.getCartByUsername(username);

        // İndirimli fiyatları ve oranları hesapla
        Map<Long, Double> discountedPrices = new HashMap<>();
        Map<Long, Double> discountRates = new HashMap<>();

        cart.getCartItems().forEach(item -> {
            double discountRate = userInteractionService.calculateCategoryDiscount(username, item.getProduct().getCategory().getId());
            double discountedPrice = item.getProduct().getPrice() * (1 - discountRate);
            discountedPrices.put(item.getProduct().getId(), discountedPrice);
            discountRates.put(item.getProduct().getId(), discountRate);
        });

        model.addAttribute("cart", cart);
        model.addAttribute("discountedPrices", discountedPrices);
        model.addAttribute("discountRates", discountRates);

        return "cart";
    }




}
