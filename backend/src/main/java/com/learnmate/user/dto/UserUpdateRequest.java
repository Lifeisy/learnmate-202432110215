package com.learnmate.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 40, message = "昵称最多 40 个字符")
        String displayName,

        @NotBlank(message = "角色不能为空")
        @Pattern(regexp = "^(ADMIN|USER)$", message = "角色只能是 ADMIN 或 USER")
        String role,

        boolean enabled,

        // 为空表示保持原密码不变
        @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,32}$",
                message = "新密码必须为 8-32 位且同时包含字母和数字")
        String password
) {
}
