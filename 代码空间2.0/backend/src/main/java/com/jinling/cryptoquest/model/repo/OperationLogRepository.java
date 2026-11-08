package com.jinling.cryptoquest.model.repo;

import com.jinling.cryptoquest.model.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
}