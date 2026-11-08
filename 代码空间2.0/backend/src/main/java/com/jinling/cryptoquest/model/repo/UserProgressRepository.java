package com.jinling.cryptoquest.model.repo;

import com.jinling.cryptoquest.model.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(Long userId);
    Optional<UserProgress> findByUserIdAndLevelId(Long userId, Long levelId);
}