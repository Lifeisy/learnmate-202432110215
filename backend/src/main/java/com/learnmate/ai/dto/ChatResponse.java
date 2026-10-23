package com.learnmate.ai.dto;

import java.util.List;

public record ChatResponse(
        String question,
        String answer,
        String mode,
        String model,
        List<AiSource> sources,
        String notice
) {
}
