package com.learnmate.ai.controller;

import com.learnmate.ai.config.AiProperties;
import com.learnmate.ai.dto.AiSource;
import com.learnmate.ai.dto.ChatRequest;
import com.learnmate.ai.dto.ChatResponse;
import com.learnmate.ai.dto.RetrievedChunk;
import com.learnmate.ai.mapper.KnowledgeChunkMapper;
import com.learnmate.ai.service.ChatService;
import com.learnmate.ai.service.LlmClient;
import com.learnmate.ai.service.PromptGuard;
import com.learnmate.ai.service.RagService;
import com.learnmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "智能问答", description = "RAG 检索增强问答与提示注入防护")
public class AiController {

    private final ChatService chatService;
    private final RagService ragService;
    private final PromptGuard promptGuard;
    private final LlmClient llmClient;
    private final AiProperties aiProperties;
    private final KnowledgeChunkMapper chunkMapper;

    public AiController(ChatService chatService,
                        RagService ragService,
                        PromptGuard promptGuard,
                        LlmClient llmClient,
                        AiProperties aiProperties,
                        KnowledgeChunkMapper chunkMapper) {
        this.chatService = chatService;
        this.ragService = ragService;
        this.promptGuard = promptGuard;
        this.llmClient = llmClient;
        this.aiProperties = aiProperties;
        this.chunkMapper = chunkMapper;
    }

    @PostMapping("/chat")
    @Operation(summary = "基于课程资料问答")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request,
                                          Authentication authentication) {
        return ApiResponse.ok(chatService.ask(authentication.getName(), request.question()));
    }

    @GetMapping("/search")
    @Operation(summary = "只检索、不生成")
    public ApiResponse<List<AiSource>> search(@RequestParam String q) {
        String question = promptGuard.validateAndNormalize(q);
        List<RetrievedChunk> chunks = ragService.search(question);
        return ApiResponse.ok(chunks.stream().map(AiSource::from).toList());
    }

    @GetMapping("/health")
    @Operation(summary = "查看 AI 配置状态")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("llmConfigured", llmClient.isConfigured());
        data.put("model", llmClient.isConfigured() ? aiProperties.model() : "local-retrieval");
        data.put("knowledgeChunks", chunkMapper.countAll());
        data.put("provider", "OpenAI-compatible");
        return ApiResponse.ok(data);
    }
}
