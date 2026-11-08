package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.model.entity.OperationLog;
import com.jinling.cryptoquest.model.repo.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作审计留痕（M7）。轻量同步写入。
 * 【后续可扩展】异步队列、AOP 切面自动采集、敏感字段脱敏。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository repository;

    public void record(Long userId, String action, String detail) {
        try {
            repository.save(OperationLog.builder()
                .userId(userId).action(action)
                .detail(detail == null ? null : detail.length() > 1024 ? detail.substring(0, 1024) : detail)
                .occurredAt(LocalDateTime.now()).build());
        } catch (Exception e) {
            log.warn("操作审计写入失败：{}", e.getMessage());
        }
    }
}