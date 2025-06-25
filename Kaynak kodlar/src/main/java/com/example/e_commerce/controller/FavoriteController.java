package com.example.e_commerce.controller;

import com.example.e_commerce.service.FavoritesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoritesService favService;

    public FavoriteController(FavoritesService favService) {
        this.favService = favService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String,Boolean>> toggle(@RequestParam Long productId,
                                                      Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = ((UserDetails)auth.getPrincipal()).getUsername();
        boolean added = favService.toggleFavorite(username, productId);
        return ResponseEntity.ok(Map.of("added", added));
    }

    @PostMapping("/remove")
    public ResponseEntity<Map<String,Boolean>> removeFavorite(@RequestParam Long productId,
                                                              Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = ((UserDetails)auth.getPrincipal()).getUsername();
        boolean removed = favService.removeFavorite(username, productId);
        return ResponseEntity.ok(Map.of("removed", removed));
    }
}

