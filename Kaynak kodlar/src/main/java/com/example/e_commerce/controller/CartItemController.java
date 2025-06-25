package com.example.e_commerce.controller;

import com.example.e_commerce.service.CartItemService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;

@Controller
@RequestMapping("/cart-item")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping("/add")
    public String addProductToCart(@RequestParam Long productId,
                                   @RequestParam Integer quantity,
                                   Authentication auth) {
        // oturum açmamışsa login sayfasına
        if (auth == null ||
                !auth.isAuthenticated() ||
                auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        String username = auth.getName();
        cartItemService.addProductToCartByUsername(username, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/delete")
    public String deleteOneCartItem(@RequestParam Long cartItemId,
                                    Authentication auth) {
        if (auth == null ||
                !auth.isAuthenticated() ||
                auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        cartItemService.deleteOneCartItem(cartItemId);
        return "redirect:/cart";
    }
}
