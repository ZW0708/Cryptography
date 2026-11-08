package com.jinling.cryptoquest.model.repo;

import com.jinling.cryptoquest.model.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop10BySessionIdOrderByIdDesc(Long sessionId);
}