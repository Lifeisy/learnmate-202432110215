package com.learnmate.auth.dto;

import com.learnmate.user.dto.UserResponse;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
}
