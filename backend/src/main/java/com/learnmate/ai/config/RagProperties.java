package com.learnmate.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "learnmate.rag")
public record RagProperties(int topK, double minScore, int chunkSize) {
}
