package com.learnmate.auth.service;

import com.learnmate.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        if (properties.secret() == null || properties.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET 至少需要 32 字节");
        }
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issue(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(properties.issuer())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.expireMinutes(), ChronoUnit.MINUTES)))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            // 拒绝非规范 Base64URL，防止末尾多一个字符仍被当成同一签名
            if (!hasCanonicalSignature(token)) {
                return false;
            }
            parse(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean hasCanonicalSignature(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            return false;
        }
        try {
            // HS384 签名是 48 字节，重新编码后逐字符比较
            byte[] signature = Decoders.BASE64URL.decode(parts[2]);
            String canonical = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(signature);
            return signature.length == 48 && canonical.equals(parts[2]);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public long expiresInSeconds() {
        return properties.expireMinutes() * 60;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
