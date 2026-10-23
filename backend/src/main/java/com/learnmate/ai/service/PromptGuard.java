package com.learnmate.ai.service;

import com.learnmate.common.BusinessException;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// 检查输入长度、归一化和常见注入指令。
@Component
public class PromptGuard {

    private static final int MAX_LENGTH = 500;

    private static final List<Pattern> BLOCKED = List.of(
            Pattern.compile("忽略(以上|之前|前面|所有).{0,10}(指令|提示|规则|设定)"),
            Pattern.compile("(system|developer)\\s*prompt|系统提示词"),
            Pattern.compile("(泄露|输出|告诉我|打印).{0,8}(密钥|api.?key|密码|token|令牌)"),
            Pattern.compile("ignore\\s+(all\\s+)?(previous|above|prior)\\s+instructions?"),
            Pattern.compile("you\\s+are\\s+now\\s+"),
            Pattern.compile("(rm\\s+-rf|drop\\s+table|delete\\s+from|truncate\\s+table)"),
            Pattern.compile("<\\|.*?\\|>")
    );

    public String validateAndNormalize(String question) {
        if (question == null || question.isBlank()) {
            throw BusinessException.badRequest("问题不能为空");
        }
        String normalized = Normalizer.normalize(question, Normalizer.Form.NFKC)
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                .trim();
        if (normalized.length() > MAX_LENGTH) {
            throw BusinessException.badRequest("问题最多 500 个字符");
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        for (Pattern pattern : BLOCKED) {
            if (pattern.matcher(lower).find()) {
                throw BusinessException.badRequest("检测到疑似提示注入或越权指令，已拒绝处理");
            }
        }
        return normalized;
    }
}
