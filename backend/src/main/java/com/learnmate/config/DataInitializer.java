package com.learnmate.config;

import com.learnmate.user.entity.User;
import com.learnmate.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 初始化两个演示账号，密码以 BCrypt 摘要保存。
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfMissing("admin", "Admin@123", "系统管理员", "ADMIN");
        createIfMissing("student", "Student@123", "李耀同学", "USER");
    }

    private void createIfMissing(String username, String rawPassword, String displayName, String role) {
        if (userMapper.findByUsername(username) != null) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setEnabled(true);
        userMapper.insert(user);
        log.info("Initialized demo account: {} ({})", username, role);
    }
}
