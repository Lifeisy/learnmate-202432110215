package com.learnmate.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$",
                message = "用户名需以字母开头，由 4-20 位字母、数字或下划线组成")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 32, message = "密码长度需为 8-32 位")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,32}$",
                message = "密码必须同时包含字母和数字")
        String password,

        @NotBlank(message = "昵称不能为空")
        @Size(max = 40, message = "昵称最多 40 个字符")
        String displayName,

        @NotBlank(message = "角色不能为空")
        @Pattern(regexp = "^(ADMIN|USER)$", message = "角色只能是 ADMIN 或 USER")
        String role,

        boolean enabled
) {
}
