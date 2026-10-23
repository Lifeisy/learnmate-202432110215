package com.learnmate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "learnmate.jwt")
public record JwtProperties(String secret, long expireMinutes, String issuer) {
}
