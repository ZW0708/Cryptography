package com.jinling.cryptoquest.api.v1;

import com.jinling.cryptoquest.common.ApiResponse;
import com.jinling.cryptoquest.model.dto.AnswerView;
import com.jinling.cryptoquest.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 答案：按当前登录用户的答错次数/通关状态，决定是否返回思路、答案与解析。
 */
@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @GetMapping
    public ApiResponse<List<AnswerView>> list(Authentication authentication) {
        return ApiResponse.ok(answerService.list(authentication.getName()));
    }
}