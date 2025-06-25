    package com.example.e_commerce.security;

    import io.jsonwebtoken.*;
    import io.jsonwebtoken.security.Keys;
    import java.security.Key;
    import java.util.Date;

    public class JwtUtil {
        private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 saat
        private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        public static String generateToken(String username) {
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key)
                    .compact();
        }

        public static String extractUsername(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        }

        public static boolean validateToken(String token) {
            try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                return false;
            }
        }
    }
