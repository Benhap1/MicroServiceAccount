package ru.skillbox.mc_account.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secretKey;

    public String getEmailFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = extractAllClaims(token, key);
            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("Unable to get email from token: {}", e.getMessage());
            return null;
        }
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        Jws<Claims> jwsClaims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return jwsClaims.getPayload();
    }
}