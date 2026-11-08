package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.model.entity.ChatMessage;
import com.jinling.cryptoquest.model.entity.ChatSession;
import com.jinling.cryptoquest.model.repo.ChatMessageRepository;
import com.jinling.cryptoquest.model.repo.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 对话会话与上下文持久化（M4）。多轮上下文取最近 10 条。
 * 【后续可扩展】会话超时清理、上下文摘要压缩、按关卡隔离会话。
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int CONTEXT_WINDOW = 10;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    @Transactional
    public ChatSession ensureSession(Long userId, Long levelId) {
        return sessionRepository.findFirstByUserIdAndLevelIdOrderByIdDesc(userId, levelId)
            .orElseGet(() -> sessionRepository.save(ChatSession.builder()
                .userId(userId).levelId(levelId).startedAt(LocalDateTime.now()).build()));
    }

    @Transactional
    public void saveMessage(Long sessionId, String role, String content) {
        messageRepository.save(ChatMessage.builder()
            .sessionId(sessionId).role(role).content(content)
            .createdAt(LocalDateTime.now()).build());
    }

    /** 最近 10 条上下文（按时间正序）供 MaaS 多轮使用。 */
    public List<Map<String, String>> recentContext(Long sessionId) {
        var recent = messageRepository.findTop10BySessionIdOrderByIdDesc(sessionId);
        java.util.Collections.reverse(recent);
        return recent.stream()
            .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
            .toList();
    }
}