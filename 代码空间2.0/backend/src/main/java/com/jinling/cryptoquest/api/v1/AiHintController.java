package com.jinling.cryptoquest.api.v1;

import com.jinling.cryptoquest.common.ApiResponse;
import com.jinling.cryptoquest.model.dto.HintRequest;
import com.jinling.cryptoquest.model.dto.HintResponse;
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
    private final AuthService authService;

    @PostMapping("/hint")
    public ApiResponse<HintResponse> hint(Authentication auth, @Valid @RequestBody HintRequest req) {
        Long userId = authService.resolveUserId(auth.getName());
        return ApiResponse.ok(aiHintService.hint(userId, req));
    }
}