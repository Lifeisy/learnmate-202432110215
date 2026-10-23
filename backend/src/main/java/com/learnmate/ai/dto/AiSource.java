package com.learnmate.ai.dto;

public record AiSource(
        String docTitle,
        String section,
        String snippet,
        double score
) {
    public static AiSource from(RetrievedChunk chunk) {
        String snippet = chunk.content().length() > 180
                ? chunk.content().substring(0, 180) + "..."
                : chunk.content();
        return new AiSource(chunk.docTitle(), chunk.section(), snippet,
                Math.round(chunk.score() * 1000.0) / 1000.0);
    }
}
