package com.learnmate.ai.dto;

public record RetrievedChunk(
        Long id,
        String docTitle,
        String section,
        String content,
        double score
) {
}
