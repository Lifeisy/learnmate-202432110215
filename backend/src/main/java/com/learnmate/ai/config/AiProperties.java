package com.learnmate.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "learnmate.ai")
public record AiProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        int timeoutSeconds,
        double temperature
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
