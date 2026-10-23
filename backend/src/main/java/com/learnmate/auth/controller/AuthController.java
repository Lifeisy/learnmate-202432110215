package com.learnmate.auth.controller;

import com.learnmate.auth.dto.LoginRequest;
import com.learnmate.auth.dto.LoginResponse;
import com.learnmate.auth.service.AuthService;
import com.learnmate.common.ApiResponse;
import com.learnmate.common.BusinessException;
import com.learnmate.user.dto.UserResponse;
import com.learnmate.user.entity.User;
import com.learnmate.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "登录与当前用户信息")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录并签发 JWT")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录状态已失效");
        }
        return ApiResponse.ok(UserResponse.from(user));
    }
}
