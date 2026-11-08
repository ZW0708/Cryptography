package com.jinling.cryptoquest.api.v1;

import com.jinling.cryptoquest.common.ApiResponse;
import com.jinling.cryptoquest.model.dto.KnowledgeItem;
import com.jinling.cryptoquest.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识库：登录用户可读取按关卡组织的知识内容（概念讲解），不含答案。
 * 思路/答案/解析见 /api/v1/answers（按答错次数或通关解锁）。
 */
@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping
    public ApiResponse<List<KnowledgeItem>> list() {
        return ApiResponse.ok(knowledgeService.listStudy());
    }
}