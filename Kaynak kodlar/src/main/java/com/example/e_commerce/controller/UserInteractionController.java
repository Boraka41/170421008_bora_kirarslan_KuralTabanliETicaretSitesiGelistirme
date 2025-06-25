package com.example.e_commerce.controller;

import com.example.e_commerce.dto.UserInteractionDto;
import com.example.e_commerce.dto.CartInteractionDto;
import com.example.e_commerce.service.UserInteractionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
public class UserInteractionController {

    private final UserInteractionService uiService;

    public UserInteractionController(UserInteractionService uiService) {
        this.uiService = uiService;
    }

    private String currentUsername(Authentication auth) {
        return auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                ? null
                : ((UserDetails) auth.getPrincipal()).getUsername();
    }

    @PostMapping("/product-click")
    public ResponseEntity<Void> productClick(@RequestBody UserInteractionDto dto,
                                             Authentication auth) {
        String user = currentUsername(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        uiService.recordProductClick(user,
                dto.getCategoryId(),
                dto.getProductId(),
                dto.getValue());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart-add")
    public ResponseEntity<Void> cartAdd(@RequestBody CartInteractionDto dto,
                                        Authentication auth) {
        String user = currentUsername(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        uiService.recordCartAdd(user,
                dto.getProductId(),
                dto.getQuantity(),
                dto.getValue());
        return ResponseEntity.ok().build();
    }
}
