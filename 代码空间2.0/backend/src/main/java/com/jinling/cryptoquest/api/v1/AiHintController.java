package com.jinling.cryptoquest.api.v1;

import com.jinling.cryptoquest.common.ApiResponse;
import com.jinling.cryptoquest.model.dto.AgentChatRequest;
import com.jinling.cryptoquest.model.dto.AgentChatResponse;
import com.jinling.cryptoquest.model.dto.HintRequest;
import com.jinling.cryptoquest.model.dto.HintResponse;
import com.jinling.cryptoquest.service.AgentChatService;
import com.jinling.cryptoquest.service.AiHintService;
import com.jinling.cryptoquest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiHintController {

    private final AiHintService aiHintService;
    private final AgentChatService agentChatService;
    private final AuthService authService;

    @PostMapping("/hint")
    public ApiResponse<HintResponse> hint(Authentication auth, @Valid @RequestBody HintRequest req) {
        Long userId = authService.resolveUserId(auth.getName());
        return ApiResponse.ok(aiHintService.hint(userId, req));
    }

    /** Agent 对话：感知关卡与交互状态，按意图调用工具后给出引导，不直接泄底。 */
    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(Authentication auth, @Valid @RequestBody AgentChatRequest req) {
        Long userId = authService.resolveUserId(auth.getName());
        return ApiResponse.ok(agentChatService.chat(userId, req));
    }
}