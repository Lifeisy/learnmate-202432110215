package com.learnmate.dashboard;

import com.learnmate.ai.mapper.ChatLogMapper;
import com.learnmate.ai.mapper.KnowledgeChunkMapper;
import com.learnmate.common.ApiResponse;
import com.learnmate.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "仪表盘", description = "首页统计数据")
public class DashboardController {

    private final UserService userService;
    private final KnowledgeChunkMapper chunkMapper;
    private final ChatLogMapper chatLogMapper;

    public DashboardController(UserService userService,
                               KnowledgeChunkMapper chunkMapper,
                               ChatLogMapper chatLogMapper) {
        this.userService = userService;
        this.chunkMapper = chunkMapper;
        this.chatLogMapper = chatLogMapper;
    }

    @GetMapping("/stats")
    @Operation(summary = "获取首页统计")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userService.countAll());
        data.put("adminUsers", userService.countAdmins());
        data.put("knowledgeChunks", chunkMapper.countAll());
        data.put("chatsToday", chatLogMapper.countToday());
        return ApiResponse.ok(data);
    }
}
