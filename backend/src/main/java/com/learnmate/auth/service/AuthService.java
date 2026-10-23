package com.learnmate.auth.service;

import com.learnmate.auth.dto.LoginRequest;
import com.learnmate.auth.dto.LoginResponse;
import com.learnmate.common.BusinessException;
import com.learnmate.user.dto.UserResponse;
import com.learnmate.user.entity.User;
import com.learnmate.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password()));
        } catch (BadCredentialsException ex) {
            // 不区分用户不存在和密码错误
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        } catch (AuthenticationException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "账号不可用，请联系管理员");
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtService.issue(user.getUsername(), user.getRole());
        return new LoginResponse(token, "Bearer", jwtService.expiresInSeconds(), UserResponse.from(user));
    }
}
