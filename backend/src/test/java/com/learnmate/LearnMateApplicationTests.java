package com.learnmate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LearnMateApplicationTests {

    @Test
    void contextLoads() {
        // 验证 Spring 容器、数据源、MyBatis、Security 配置可正常装配
    }
}
