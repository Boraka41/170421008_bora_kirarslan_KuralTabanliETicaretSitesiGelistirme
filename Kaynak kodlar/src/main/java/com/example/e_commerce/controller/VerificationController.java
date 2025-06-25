package com.example.e_commerce.controller;

import com.example.e_commerce.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {

    private final UserService userService;

    public VerificationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        String resultMessage = userService.verifyUser(token);

        if (resultMessage.equals("Hesabınız doğrulandı, giriş yapabilirsiniz.")) {
            return ResponseEntity.ok(resultMessage);
        } else {
            return ResponseEntity.badRequest().body(resultMessage);
        }
    }
}
