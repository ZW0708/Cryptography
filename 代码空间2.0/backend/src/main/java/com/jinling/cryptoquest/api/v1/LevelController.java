package com.jinling.cryptoquest.api.v1;

import com.jinling.cryptoquest.common.ApiResponse;
import com.jinling.cryptoquest.model.dto.*;
import com.jinling.cryptoquest.service.AuthService;
import com.jinling.cryptoquest.service.LevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;
    private final AuthService authService;

    @GetMapping("/overview")
    public ApiResponse<ProgressOverview> overview(Authentication auth) {
        Long userId = authService.resolveUserId(auth.getName());
        return ApiResponse.ok(levelService.overview(userId));
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<SummaryCardView> summary(@PathVariable Long id) {
        return ApiResponse.ok(levelService.summary(id));
    }

    @PostMapping("/submit")
    public ApiResponse<Boolean> submit(Authentication auth, @Valid @RequestBody AnswerSubmitRequest req) {
        Long userId = authService.resolveUserId(auth.getName());
        return ApiResponse.ok(levelService.submitAnswer(userId, req));
    }
}