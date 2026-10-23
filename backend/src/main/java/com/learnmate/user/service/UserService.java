package com.learnmate.user.service;

import com.learnmate.common.BusinessException;
import com.learnmate.user.dto.UserCreateRequest;
import com.learnmate.user.dto.UserResponse;
import com.learnmate.user.dto.UserUpdateRequest;
import com.learnmate.user.entity.User;
import com.learnmate.user.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> list(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        return userMapper.findAll(normalized).stream().map(UserResponse::from).toList();
    }

    public UserResponse get(Long id) {
        return UserResponse.from(requireUser(id));
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userMapper.findByUsername(request.username()) != null) {
            throw BusinessException.conflict("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        userMapper.insert(user);
        return UserResponse.from(requireUser(user.getId()));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = requireUser(id);
        String currentUsername = currentUsername();
        boolean lastAdmin = "ADMIN".equals(user.getRole()) && userMapper.countByRole("ADMIN") <= 1;
        if (lastAdmin && (!"ADMIN".equals(request.role()) || !request.enabled())) {
            throw BusinessException.conflict("系统至少需要保留一个启用的管理员");
        }
        if (user.getUsername().equals(currentUsername)
                && (!"ADMIN".equals(request.role()) || !request.enabled())) {
            throw BusinessException.badRequest("不能停用或降级当前登录的管理员账号");
        }
        user.setDisplayName(request.displayName().trim());
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        if (StringUtils.hasText(request.password())) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        userMapper.update(user);
        return UserResponse.from(requireUser(id));
    }

    @Transactional
    public void delete(Long id) {
        User user = requireUser(id);
        if (user.getUsername().equals(currentUsername())) {
            throw BusinessException.badRequest("不能删除当前登录账号");
        }
        if ("ADMIN".equals(user.getRole()) && userMapper.countByRole("ADMIN") <= 1) {
            throw BusinessException.conflict("系统至少需要保留一个管理员");
        }
        userMapper.deleteById(id);
    }

    public int countAll() {
        return userMapper.countAll();
    }

    public int countAdmins() {
        return userMapper.countByRole("ADMIN");
    }

    private User requireUser(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "" : authentication.getName();
    }
}
