package com.jinling.cryptoquest.model.repo;

import com.jinling.cryptoquest.model.entity.ClearanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClearanceRecordRepository extends JpaRepository<ClearanceRecord, Long> {
    List<ClearanceRecord> findByUserId(Long userId);
}