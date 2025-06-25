package com.example.e_commerce.controller;

import com.example.e_commerce.dto.LoginRequest;
import com.example.e_commerce.dto.UserDto;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.EmailService;
import com.example.e_commerce.service.UserService;
import com.example.e_commerce.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private EmailService emailService;

    public AuthController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String token = JwtUtil.generateToken(loginRequest.getUsername());

                Cookie jwtCookie = new Cookie("jwtToken", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(false);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(60 * 60); // 1 saat

                response.addCookie(jwtCookie);

                return ResponseEntity.ok("Giriş başarılı");
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş başarısız");
            }
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş başarısız");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@ModelAttribute UserDto userDto, HttpServletRequest request){
        User newUser = userService.registerNewUser(userDto);

        String siteUrl = getSiteURL(request);
        emailService.sendVerificationEmail(newUser, siteUrl);

        return ResponseEntity.ok("Kayıt başarılı. Lütfen e-postanızı kontrol edin ve hesabınızı doğrulayın.");
    }

    private String getSiteURL(HttpServletRequest request) {
        return request.getRequestURL().toString().replace(request.getServletPath(), "");
    }


}
