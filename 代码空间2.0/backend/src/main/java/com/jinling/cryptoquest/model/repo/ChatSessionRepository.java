package com.jinling.cryptoquest.model.repo;

import com.jinling.cryptoquest.model.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findFirstByUserIdAndLevelIdOrderByIdDesc(Long userId, Long levelId);
}